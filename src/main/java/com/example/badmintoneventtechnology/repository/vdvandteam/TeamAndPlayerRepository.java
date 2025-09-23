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
    public Map<String, Integer> loadSinglesNames(int idNoiDung, int idGiaiDau) {
        Map<String, Integer> nameToId = new LinkedHashMap<>();
        final String sql = "SELECT DISTINCT v.ID, COALESCE(v.HO_TEN, CAST(d.ID_VDV AS VARCHAR)) AS TEN " +
                "FROM DANG_KI_CA_NHAN d " +
                "LEFT JOIN VAN_DONG_VIEN v ON v.ID = d.ID_VDV " +
                "WHERE d.ID_NOI_DUNG = ? AND d.ID_GIAI_DAU = ? " +
                "ORDER BY TEN";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNoiDung);
            ps.setInt(2, idGiaiDau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vdvId = rs.getInt("ID");
                    String ten = rs.getString("TEN");
                    if (ten != null && !ten.isBlank()) {
                        nameToId.put(ten, vdvId);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Lỗi tải VĐV đăng ký cá nhân: " + ex.getMessage(),
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }
        return nameToId;
    }

    /** Tương thích ngược, nếu code cũ vẫn gọi bằng KNR. */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKnr(int knrAsKatnr, int vernr) {
        return loadSinglesNames(knrAsKatnr, vernr);
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
