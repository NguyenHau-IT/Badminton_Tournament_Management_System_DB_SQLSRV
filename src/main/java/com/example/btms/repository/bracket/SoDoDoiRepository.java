package com.example.btms.repository.bracket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.bracket.SoDoDoi;

public class SoDoDoiRepository {
    private final Connection conn;

    public SoDoDoiRepository(Connection conn) { this.conn = conn; }

    /** CREATE */
    public int add(SoDoDoi row) {
        final String sql = """
            INSERT INTO SO_DO_DOI
                (ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, TOA_DO_X, TOA_DO_Y,
                 VI_TRI, SO_DO, THOI_GIAN, DIEM, ID_TRAN_DAU)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, row.getIdGiai());
            ps.setInt(2, row.getIdNoiDung());
            if (row.getIdClb() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, row.getIdClb());
            ps.setString(4, row.getTenTeam());
            if (row.getToaDoX() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, row.getToaDoX());
            if (row.getToaDoY() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, row.getToaDoY());
            ps.setInt(7, row.getViTri());
            if (row.getSoDo() == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, row.getSoDo());
            if (row.getThoiGian() == null) ps.setNull(9, Types.TIMESTAMP);
            else ps.setTimestamp(9, Timestamp.valueOf(row.getThoiGian()));
            if (row.getDiem() == null) ps.setNull(10, Types.INTEGER); else ps.setInt(10, row.getDiem());
            if (row.getIdTranDau() == null) ps.setNull(11, Types.INTEGER); else ps.setInt(11, row.getIdTranDau());

            int affected = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return affected;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm SO_DO_DOI", e);
        }
    }

    /** READ one */
    public SoDoDoi findOne(int idGiai, int idNoiDung, int viTri) {
        final String sql = "SELECT * FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm SO_DO_DOI", e);
        }
    }

    /** LIST by bracket */
    public List<SoDoDoi> list(int idGiai, int idNoiDung) {
        final String sql = "SELECT * FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY VI_TRI";
        final List<SoDoDoi> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê SO_DO_DOI", e);
        }
        return list;
    }

    /** UPDATE full row (ten, tọa độ, sơ đồ, thời gian, điểm, id trận) */
    public void update(SoDoDoi row) {
        final String sql = """
            UPDATE SO_DO_DOI
               SET ID_CLB=?, TEN_TEAM=?, TOA_DO_X=?, TOA_DO_Y=?, SO_DO=?,
                   THOI_GIAN=?, DIEM=?, ID_TRAN_DAU=?
             WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (row.getIdClb() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, row.getIdClb());
            ps.setString(2, row.getTenTeam());
            if (row.getToaDoX() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, row.getToaDoX());
            if (row.getToaDoY() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, row.getToaDoY());
            if (row.getSoDo() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, row.getSoDo());
            if (row.getThoiGian() == null) ps.setNull(6, Types.TIMESTAMP);
            else ps.setTimestamp(6, Timestamp.valueOf(row.getThoiGian()));
            if (row.getDiem() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, row.getDiem());
            if (row.getIdTranDau() == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, row.getIdTranDau());

            ps.setInt(9, row.getIdGiai());
            ps.setInt(10, row.getIdNoiDung());
            ps.setInt(11, row.getViTri());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật SO_DO_DOI", e);
        }
    }

    /** PATCH tiện dụng: chỉ đổi điểm */
    public int updateDiem(int idGiai, int idNoiDung, int viTri, Integer diem) {
        final String sql = "UPDATE SO_DO_DOI SET DIEM=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (diem == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, diem);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, viTri);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật điểm", e);
        }
    }

    /** PATCH: gán/huỷ liên kết trận đấu */
    public int updateTranDau(int idGiai, int idNoiDung, int viTri, Integer idTranDau) {
        final String sql = "UPDATE SO_DO_DOI SET ID_TRAN_DAU=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idTranDau == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, idTranDau);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, viTri);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật ID_TRAN_DAU", e);
        }
    }

    /** DELETE */
    public void delete(int idGiai, int idNoiDung, int viTri) {
        final String sql = "DELETE FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa SO_DO_DOI", e);
        }
    }

    private SoDoDoi map(ResultSet rs) throws SQLException {
        return new SoDoDoi(
            rs.getInt("ID_GIAI"),
            rs.getInt("ID_NOI_DUNG"),
            rs.getObject("ID_CLB") != null ? rs.getInt("ID_CLB") : null,
            rs.getString("TEN_TEAM"),
            rs.getObject("TOA_DO_X") != null ? rs.getInt("TOA_DO_X") : null,
            rs.getObject("TOA_DO_Y") != null ? rs.getInt("TOA_DO_Y") : null,
            rs.getInt("VI_TRI"),
            rs.getObject("SO_DO") != null ? rs.getInt("SO_DO") : null,
            rs.getTimestamp("THOI_GIAN") != null ? rs.getTimestamp("THOI_GIAN").toLocalDateTime() : null,
            rs.getObject("DIEM") != null ? rs.getInt("DIEM") : null,                 // NEW
            rs.getObject("ID_TRAN_DAU") != null ? rs.getInt("ID_TRAN_DAU") : null    // NEW
        );
    }
}
