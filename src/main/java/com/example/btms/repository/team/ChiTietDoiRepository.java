package com.example.btms.repository.team;

import com.example.btms.model.team.ChiTietDoi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDoiRepository {
    private final Connection conn;

    public ChiTietDoiRepository(Connection conn) {
        this.conn = conn;
    }

    public void addMember(int idTeam, int idVdv) {
        final String sql = "INSERT INTO CHI_TIET_DOI (ID_TEAM, ID_VDV) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            ps.setInt(2, idVdv);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm thành viên đội", e);
        }
    }

    public void addMembers(int idTeam, List<Integer> vdvIds) {
        final String sql = "INSERT INTO CHI_TIET_DOI (ID_TEAM, ID_VDV) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer v : vdvIds) {
                ps.setInt(1, idTeam);
                ps.setInt(2, v);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm nhiều thành viên", e);
        }
    }

    public List<ChiTietDoi> findByTeam(int idTeam) {
        final String sql = "SELECT ID_TEAM, ID_VDV FROM CHI_TIET_DOI WHERE ID_TEAM=?";
        List<ChiTietDoi> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(new ChiTietDoi(rs.getInt(1), rs.getInt(2)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy thành viên đội", e);
        }
        return out;
    }

    public void removeMember(int idTeam, int idVdv) {
        final String sql = "DELETE FROM CHI_TIET_DOI WHERE ID_TEAM=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            ps.setInt(2, idVdv);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa thành viên đội", e);
        }
    }

    public void removeAll(int idTeam) {
        final String sql = "DELETE FROM CHI_TIET_DOI WHERE ID_TEAM=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTeam);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa tất cả thành viên đội", e);
        }
    }
}
