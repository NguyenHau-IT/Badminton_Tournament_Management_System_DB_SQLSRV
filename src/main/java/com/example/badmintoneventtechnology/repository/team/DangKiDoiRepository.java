package com.example.badmintoneventtechnology.repository.team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.example.badmintoneventtechnology.model.team.DangKiDoi;

public class DangKiDoiRepository {
    private final Connection conn;

    public DangKiDoiRepository(Connection conn) {
        this.conn = conn;
    }

    public DangKiDoi add(DangKiDoi t) {
        final String sql = "INSERT INTO DANG_KI_DOI (ID_GIAI, ID_NOI_DUNG, ID_CAU_LAC_BO, TEN_TEAM) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdGiai());
            ps.setInt(2, t.getIdNoiDung());
            if (t.getIdCauLacBo() != null)
                ps.setInt(3, t.getIdCauLacBo());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setString(4, t.getTenTeam());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    t.setIdTeam(rs.getInt(1));
            }
            return t;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đội", e);
        }
    }

    public DangKiDoi findById(int idTeam) {
        final String sql = "SELECT ID_TEAM, ID_GIAI, ID_NOI_DUNG, ID_CAU_LAC_BO, TEN_TEAM " +
                "FROM DANG_KI_DOI WHERE ID_TEAM = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DangKiDoi(
                            rs.getInt("ID_TEAM"),
                            rs.getInt("ID_GIAI"),
                            rs.getInt("ID_NOI_DUNG"),
                            rs.getObject("ID_CAU_LAC_BO") != null ? rs.getInt("ID_CAU_LAC_BO") : null,
                            rs.getString("TEN_TEAM"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đội id=" + idTeam, e);
        }
    }

    public List<DangKiDoi> findAllBy(int idGiai, int idNoiDung) {
        final String sql = "SELECT ID_TEAM, ID_GIAI, ID_NOI_DUNG, ID_CAU_LAC_BO, TEN_TEAM " +
                "FROM DANG_KI_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY TEN_TEAM";
        List<DangKiDoi> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new DangKiDoi(
                            rs.getInt("ID_TEAM"),
                            rs.getInt("ID_GIAI"),
                            rs.getInt("ID_NOI_DUNG"),
                            rs.getObject("ID_CAU_LAC_BO") != null ? rs.getInt("ID_CAU_LAC_BO") : null,
                            rs.getString("TEN_TEAM")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách đội", e);
        }
        return out;
    }

    public void update(DangKiDoi t) {
        final String sql = "UPDATE DANG_KI_DOI SET ID_GIAI=?, ID_NOI_DUNG=?, ID_CAU_LAC_BO=?, TEN_TEAM=? " +
                "WHERE ID_TEAM=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getIdGiai());
            ps.setInt(2, t.getIdNoiDung());
            if (t.getIdCauLacBo() != null)
                ps.setInt(3, t.getIdCauLacBo());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setString(4, t.getTenTeam());
            ps.setInt(5, t.getIdTeam());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đội id=" + t.getIdTeam(), e);
        }
    }

    public void delete(int idTeam) {
        final String sql = "DELETE FROM DANG_KI_DOI WHERE ID_TEAM=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa đội id=" + idTeam, e);
        }
    }
}
