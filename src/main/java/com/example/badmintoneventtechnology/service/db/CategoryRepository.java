package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.config.Prefs;

public class CategoryRepository {
    private final Connection conn;

    public CategoryRepository(Connection conn) {
        this.conn = conn;
    }

    /** Returns [singles, doubles] maps of KATBEZ->KNR */
    public Map<String, Integer>[] loadCategories() {
        Map<String, Integer> singles = new LinkedHashMap<>();
        Map<String, Integer> doubles = new LinkedHashMap<>();
        if (conn == null)
            return new Map[] { singles, doubles };
        int vernr = new Prefs().getInt("selectedTournamentVernr", -1);

        String sql = "SELECT k.KNR, k.KATBEZ, k.TEAM " +
                "FROM PUBLIC.KATEGORIE k " +
                "JOIN PUBLIC.VERANSTALTUNGKAT vk ON vk.KNR = k.KNR " +
                "WHERE vk.VERNR = " + vernr + " " +
                "ORDER BY k.KNR ASC";

        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int knr = rs.getInt(1);
                String kat = rs.getString(2);
                String team = rs.getString(3);
                boolean isTeam = team != null && team.trim().equalsIgnoreCase("T");
                if (kat != null && !kat.isBlank()) {
                    (isTeam ? doubles : singles).put(kat, knr);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Tải \"nội dung\" từ DB lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }

        return new Map[] { singles, doubles };
    }
}