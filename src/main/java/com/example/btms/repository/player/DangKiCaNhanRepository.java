package com.example.btms.repository.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.player.DangKiCaNhan;

public class DangKiCaNhanRepository {
    private final Connection conn;

    public DangKiCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    /** CREATE */
    public void add(DangKiCaNhan r) {
        final String sql = """
                INSERT INTO DANG_KI_CA_NHAN
                    (ID_GIAI, ID_NOI_DUNG, ID_VDV, THOI_GIAN_TAO, KIEM_TRA)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdVdv());
            ps.setTimestamp(4, Timestamp.valueOf(
                    r.getThoiGianTao() != null ? r.getThoiGianTao() : LocalDateTime.now()));
            // nếu cột là BIT/TINYINT(1): dùng setBoolean; nếu muốn giữ null thì setObject
            ps.setObject(5, r.getKiemTra() != null ? (r.getKiemTra() ? 1 : 0) : 0, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm DANG_KI_CA_NHAN", e);
        }
    }

    /** READ */
    public DangKiCaNhan findOne(int idGiai, int idNoiDung, int idVdv) {
        final String sql = "SELECT * FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đăng ký cá nhân", e);
        }
    }

    /** LIST by giải + nội dung (tuỳ chọn lọc KIEM_TRA) */
    public List<DangKiCaNhan> list(int idGiai, int idNoiDung, Boolean kiemTra) {
        final String base = "SELECT * FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=?";
        final String order = " ORDER BY THOI_GIAN_TAO";
        final String sql = kiemTra == null ? (base + order) : (base + " AND KIEM_TRA=?" + order);

        final List<DangKiCaNhan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            if (kiemTra != null)
                ps.setInt(3, kiemTra ? 1 : 0);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê đăng ký cá nhân", e);
        }
        return out;
    }

    /** UPDATE KIEM_TRA */
    public int updateKiemTra(int idGiai, int idNoiDung, int idVdv, boolean kiemTra) {
        final String sql = "UPDATE DANG_KI_CA_NHAN SET KIEM_TRA=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kiemTra ? 1 : 0);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, idVdv);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật KIEM_TRA", e);
        }
    }

    /** DELETE */
    public void delete(int idGiai, int idNoiDung, int idVdv) {
        final String sql = "DELETE FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa đăng ký cá nhân", e);
        }
    }

    /** DELETE ALL by tournament */
    public int deleteAllByGiai(int idGiai) {
        final String sql = "DELETE FROM DANG_KI_CA_NHAN WHERE ID_GIAI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa tất cả đăng ký cá nhân của giải", e);
        }
    }

    /** DELETE ALL by tournament and content */
    public int deleteAllByGiaiAndNoiDung(int idGiai, int idNoiDung) {
        final String sql = "DELETE FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa tất cả đăng ký cá nhân của nội dung trong giải", e);
        }
    }

    private DangKiCaNhan map(ResultSet rs) throws SQLException {
        final Timestamp ts = rs.getTimestamp("THOI_GIAN_TAO");
        // KIEM_TRA có thể là BIT/TINYINT(1) => đọc về int/boolean
        Object ktObj = rs.getObject("KIEM_TRA");
        Boolean kt = (ktObj == null) ? null : (rs.getInt("KIEM_TRA") != 0);

        return new DangKiCaNhan(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"),
                ts != null ? ts.toLocalDateTime() : null,
                kt != null ? kt : Boolean.FALSE);
    }
}
