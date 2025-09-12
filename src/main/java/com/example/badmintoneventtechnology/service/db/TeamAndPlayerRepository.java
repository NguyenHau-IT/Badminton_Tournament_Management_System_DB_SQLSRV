package com.example.badmintoneventtechnology.service.db;

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

    /**
     * Lay VDV don da dang ky trong su kien (su_kien_id) theo DB khong dau (MySQL).
     */
    public Map<String, Integer> loadSinglesNamesBySuKienId(int suKienId) {
        Map<String, Integer> nameToId = new LinkedHashMap<>();
        final String sql = "SELECT DISTINCT v.vdv_id AS VID, CONCAT(v.ho, ' ', v.ten) AS NM " +
                "FROM dangky dk " +
                "JOIN vdv v ON v.vdv_id = dk.vdv_id " +
                "WHERE dk.su_kien_id = ? AND dk.loai = 'DON' AND dk.trang_thai = 'DUYET' " +
                "ORDER BY NM";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, suKienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vid = rs.getInt("VID");
                    String nm = rs.getString("NM");
                    if (nm != null && !nm.isBlank()) {
                        nameToId.put(nm, vid);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tai VDV (don) loi: " + ex.getMessage(),
                    "Loi DB", JOptionPane.ERROR_MESSAGE);
        }
        return nameToId;
    }

    /** Tuong thich nguoc voi ten cu (KATNR) -> thuc chat la su_kien_id. */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKatnr(int katnrAsSuKienId) {
        return loadSinglesNamesBySuKienId(katnrAsSuKienId);
    }

    /** Tuong thich nguoc (KNR) -> thuc chat la su_kien_id. */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKnr(int knrAsSuKienId) {
        return loadSinglesNamesBySuKienId(knrAsSuKienId);
    }

    /** Danh sach cap doi (pair) trong su kien (su_kien_id). */
    public List<TeamItem> fetchPairsBySuKienId(int suKienId) {
        List<TeamItem> list = new ArrayList<>();
        final String sql = "SELECT cd.capdoi_id AS TEAMID, " +
                "       CONCAT(v1.ho, ' ', v1.ten, ' / ', v2.ho, ' ', v2.ten) AS LBL " +
                "FROM capdoi cd " +
                "JOIN vdv v1 ON v1.vdv_id = cd.vdv1_id " +
                "JOIN vdv v2 ON v2.vdv_id = cd.vdv2_id " +
                "WHERE cd.su_kien_id = ? " +
                "ORDER BY LBL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, suKienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TeamItem(rs.getInt("TEAMID"), rs.getString("LBL")));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Doc danh sach cap doi loi: " + ex.getMessage(),
                    "Loi DB", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /** Tuong thich nguoc: ten cu fetchTeamsByKnr -> dung su_kien_id. */
    @Deprecated
    public List<TeamItem> fetchTeamsByKnr(int knrAsSuKienId) {
        return fetchPairsBySuKienId(knrAsSuKienId);
    }

    /** Lay toi da 2 VDV cua 1 cap doi (teamId = capdoi_id). */
    public Player[] fetchTeamPlayersDetailed(int teamId) {
        List<Player> players = new ArrayList<>();
        final String sql =
                // Lay VDV thu 1
                "SELECT v.vdv_id AS VID, CONCAT(v.ho, ' ', v.ten) AS NM " +
                        "FROM capdoi cd " +
                        "JOIN vdv v ON v.vdv_id = cd.vdv1_id " +
                        "WHERE cd.capdoi_id = ? " +
                        "UNION ALL " +
                        // Lay VDV thu 2
                        "SELECT v.vdv_id AS VID, CONCAT(v.ho, ' ', v.ten) AS NM " +
                        "FROM capdoi cd " +
                        "JOIN vdv v ON v.vdv_id = cd.vdv2_id " +
                        "WHERE cd.capdoi_id = ? ";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(new Player(rs.getInt("VID"), rs.getString("NM")));
                    if (players.size() == 2)
                        break;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Doc VDV theo capdoi_id loi: " + ex.getMessage(),
                    "Loi DB", JOptionPane.ERROR_MESSAGE);
        }
        return players.toArray(new Player[0]);
    }
}
