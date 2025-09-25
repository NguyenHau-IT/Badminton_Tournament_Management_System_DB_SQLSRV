package com.example.btms.repository.player;

import com.example.btms.model.player.DangKiCaNhan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DangKiCaNhanRepository {
    private final Connection conn;

    public DangKiCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    /** CREATE */
    public void add(DangKiCaNhan r) {
        String sql = "INSERT INTO DANG_KI_CA_NHAN (ID_GIAI, ID_NOI_DUNG, ID_VDV, THOI_GIAN_TAO) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdVdv());
            ps.setTimestamp(4, r.getThoiGianTao() != null ? Timestamp.valueOf(r.getThoiGianTao())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm DANG_KI_CA_NHAN", e);
        }
    }

    /** READ */
    public DangKiCaNhan findOne(int idGiai, int idNoiDung, int idVdv) {
        String sql = "SELECT * FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
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

    /** LIST by giải + nội dung */
    public List<DangKiCaNhan> list(int idGiai, int idNoiDung) {
        String sql = "SELECT * FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY THOI_GIAN_TAO";
        List<DangKiCaNhan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê đăng ký cá nhân", e);
        }
        return out;
    }

    /** DELETE */
    public void delete(int idGiai, int idNoiDung, int idVdv) {
        String sql = "DELETE FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa đăng ký cá nhân", e);
        }
    }

    private DangKiCaNhan map(ResultSet rs) throws SQLException {
        return new DangKiCaNhan(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"),
                rs.getTimestamp("THOI_GIAN_TAO") != null ? rs.getTimestamp("THOI_GIAN_TAO").toLocalDateTime() : null);
    }
}
