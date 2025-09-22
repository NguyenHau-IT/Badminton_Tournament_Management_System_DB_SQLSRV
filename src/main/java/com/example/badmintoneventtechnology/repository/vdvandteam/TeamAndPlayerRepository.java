package com.example.badmintoneventtechnology.repository.vdvandteam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.model.match.Player;
import com.example.badmintoneventtechnology.model.match.TeamItem;

public class TeamAndPlayerRepository {
    private final Connection conn;

    public TeamAndPlayerRepository(Connection conn) {
        this.conn = conn;
    }

    /** Lấy VĐV đơn theo KATNR (bảng NENNUNGENEINZEL). */
    public Map<String, Integer> loadSinglesNamesByKatnr(int katnr, int vernr) {
        Map<String, Integer> nameToId = new LinkedHashMap<>();
        final String sql = "SELECT DISTINCT e.NNR, COALESCE(n.NAME, CAST(e.NNR AS VARCHAR)) AS NM " +
                "FROM PUBLIC.NENNUNGENEINZEL e " +
                "LEFT JOIN PUBLIC.NAMES n ON n.NNR = e.NNR " +
                "WHERE e.KATNR = ? AND e.VERNR = ?" +
                "ORDER BY NM";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, katnr);
            ps.setInt(2, vernr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nnr = rs.getInt("NNR");
                    String nm = rs.getString("NM");
                    if (nm != null && !nm.isBlank()) {
                        nameToId.put(nm, nnr);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tải VĐV (đơn) lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return nameToId;
    }

    /** Tương thích ngược, nếu code cũ vẫn gọi bằng KNR. */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKnr(int knrAsKatnr, int vernr) {
        return loadSinglesNamesByKatnr(knrAsKatnr, vernr);
    }

    /** Danh sách đội theo KNR (bảng NENNUNGENTEAM). */
    public List<TeamItem> fetchTeamsByKnr(int knr, int vernr) {
        List<TeamItem> list = new ArrayList<>();
        final String sql = "SELECT DISTINCT COALESCE(ne.MANNSCHAFT, CAST(ne.TEAMID AS VARCHAR)) AS LBL, ne.TEAMID " +
                "FROM PUBLIC.NENNUNGENTEAM ne " +
                "WHERE ne.KNR = ? AND ne.VERNR = ?" +
                "ORDER BY LBL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, knr);
            ps.setInt(2, vernr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TeamItem(rs.getInt("TEAMID"), rs.getString("LBL")));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Đọc danh sách đội lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /** Lấy tối đa 2 VĐV của 1 team. */
    public Player[] fetchTeamPlayersDetailed(int teamId) {
        List<Player> players = new ArrayList<>();
        final String sql = "SELECT t.NNR, COALESCE(n.NAME, CAST(t.NNR AS VARCHAR)) AS NM " +
                "FROM PUBLIC.TEAM t " +
                "LEFT JOIN PUBLIC.NAMES n ON n.NNR = t.NNR " +
                "WHERE t.TEAMID = ? " +
                "ORDER BY t.NNR";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(new Player(rs.getInt("NNR"), rs.getString("NM")));
                    if (players.size() == 2)
                        break;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Đọc VĐV theo TEAMID lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return players.toArray(new Player[0]);
    }
}
