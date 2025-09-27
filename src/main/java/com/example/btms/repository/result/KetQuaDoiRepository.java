package com.example.btms.repository.result;

import com.example.btms.model.result.KetQuaDoi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KetQuaDoiRepository {
    private final Connection conn;

    public KetQuaDoiRepository(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void add(KetQuaDoi r) {
        final String sql = "INSERT INTO KET_QUA_DOI (ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, THU_HANG) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdClb());
            ps.setString(4, r.getTenTeam());
            ps.setInt(5, r.getThuHang());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm KET_QUA_DOI", e);
        }
    }

    // READ
    public KetQuaDoi findOne(int idGiai, int idNoiDung, int thuHang) {
        final String sql = "SELECT * FROM KET_QUA_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuHang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm KET_QUA_DOI", e);
        }
    }

    // LIST
    public List<KetQuaDoi> list(int idGiai, int idNoiDung) {
        final String sql = "SELECT * FROM KET_QUA_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY THU_HANG";
        List<KetQuaDoi> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi list KET_QUA_DOI", e);
        }
        return out;
    }

    // UPDATE
    public void update(KetQuaDoi r) {
        final String sql = "UPDATE KET_QUA_DOI SET ID_CLB=?, TEN_TEAM=? " +
                "WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdClb());
            ps.setString(2, r.getTenTeam());
            ps.setInt(3, r.getIdGiai());
            ps.setInt(4, r.getIdNoiDung());
            ps.setInt(5, r.getThuHang());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update KET_QUA_DOI", e);
        }
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int thuHang) {
        final String sql = "DELETE FROM KET_QUA_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuHang);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi delete KET_QUA_DOI", e);
        }
    }

    private KetQuaDoi map(ResultSet rs) throws SQLException {
        return new KetQuaDoi(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_CLB"),
                rs.getString("TEN_TEAM"),
                rs.getInt("THU_HANG"));
    }
}
