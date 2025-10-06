package com.example.btms.repository.bracket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.bracket.SoDoCaNhan;

public class SoDoCaNhanRepository {
    private final Connection conn;

    public SoDoCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    /** CREATE */
    public void add(SoDoCaNhan r) {
        final String sql = """
                INSERT INTO SO_DO_CA_NHAN
                  (ID_GIAI, ID_NOI_DUNG, ID_VDV, TOA_DO_X, TOA_DO_Y,
                   VI_TRI, SO_DO, THOI_GIAN, DIEM, ID_TRAN_DAU)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdVdv());
            ps.setInt(4, r.getToaDoX());
            ps.setInt(5, r.getToaDoY());
            ps.setInt(6, r.getViTri());
            ps.setInt(7, r.getSoDo());
            ps.setTimestamp(8, Timestamp.valueOf(r.getThoiGian()));
            if (r.getDiem() == null)
                ps.setNull(9, Types.INTEGER);
            else
                ps.setInt(9, r.getDiem());
            if (r.getIdTranDau() == null)
                ps.setNull(10, Types.VARCHAR);
            else
                ps.setString(10, r.getIdTranDau());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm SO_DO_CA_NHAN", e);
        }
    }

    /** READ one */
    public SoDoCaNhan findOne(int idGiai, int idNoiDung, int viTri) {
        final String sql = "SELECT * FROM SO_DO_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm SO_DO_CA_NHAN", e);
        }
    }

    /** LIST */
    public List<SoDoCaNhan> list(int idGiai, int idNoiDung) {
        final String sql = "SELECT * FROM SO_DO_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY VI_TRI";
        final List<SoDoCaNhan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi list SO_DO_CA_NHAN", e);
        }
        return out;
    }

    /** UPDATE full row */
    public void update(SoDoCaNhan r) {
        final String sql = """
                UPDATE SO_DO_CA_NHAN
                   SET ID_VDV=?, TOA_DO_X=?, TOA_DO_Y=?, SO_DO=?, THOI_GIAN=?, DIEM=?, ID_TRAN_DAU=?
                 WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdVdv());
            ps.setInt(2, r.getToaDoX());
            ps.setInt(3, r.getToaDoY());
            ps.setInt(4, r.getSoDo());
            ps.setTimestamp(5, Timestamp.valueOf(r.getThoiGian()));
            if (r.getDiem() == null)
                ps.setNull(6, Types.INTEGER);
            else
                ps.setInt(6, r.getDiem());
            if (r.getIdTranDau() == null)
                ps.setNull(7, Types.VARCHAR);
            else
                ps.setString(7, r.getIdTranDau());

            ps.setInt(8, r.getIdGiai());
            ps.setInt(9, r.getIdNoiDung());
            ps.setInt(10, r.getViTri());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update SO_DO_CA_NHAN", e);
        }
    }

    /** PATCH: chỉ đổi điểm */
    public int updateDiem(int idGiai, int idNoiDung, int viTri, Integer diem) {
        final String sql = "UPDATE SO_DO_CA_NHAN SET DIEM=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (diem == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, diem);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, viTri);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật điểm", e);
        }
    }

    /** PATCH: gán/hủy liên kết trận đấu */
    public int updateTranDau(int idGiai, int idNoiDung, int viTri, String idTranDau) {
        final String sql = "UPDATE SO_DO_CA_NHAN SET ID_TRAN_DAU=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idTranDau == null)
                ps.setNull(1, Types.VARCHAR);
            else
                ps.setString(1, idTranDau);
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
        final String sql = "DELETE FROM SO_DO_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi delete SO_DO_CA_NHAN", e);
        }
    }

    private SoDoCaNhan map(ResultSet rs) throws SQLException {
        return new SoDoCaNhan(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"),
                rs.getInt("TOA_DO_X"),
                rs.getInt("TOA_DO_Y"),
                rs.getInt("VI_TRI"),
                rs.getInt("SO_DO"),
                rs.getTimestamp("THOI_GIAN").toLocalDateTime(),
                rs.getObject("DIEM") != null ? rs.getInt("DIEM") : null, // NEW
                rs.getString("ID_TRAN_DAU") != null ? rs.getString("ID_TRAN_DAU") : null // NEW
        );
    }
}
