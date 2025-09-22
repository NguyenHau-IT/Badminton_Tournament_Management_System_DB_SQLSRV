package com.example.badmintoneventtechnology.repository.category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.config.Prefs;

public class CategoryRepository {
    private final Connection conn;

    public CategoryRepository(Connection conn) {
        this.conn = conn;
    }

    /** Returns [singles, doubles] maps of TEN_NOI_DUNG->ID */
    public Map<String, Integer>[] loadCategories() {
        Map<String, Integer> singles = new LinkedHashMap<>();
        Map<String, Integer> doubles = new LinkedHashMap<>();
        if (conn == null)
            return new Map[] { singles, doubles };

        int vernr = new Prefs().getInt("selectedTournamentVernr", -1);
        if (vernr < 0)
            return new Map[] { singles, doubles };

        String sql = "SELECT nd.ID, nd.TEN_NOI_DUNG, nd.TEAM " +
                "FROM NOI_DUNG nd " +
                "JOIN CHI_TIET_GIAI_DAU ctgd ON ctgd.ID_NOI_DUNG = nd.ID " +
                "WHERE ctgd.ID_GIAI_DAU = ? " +
                "ORDER BY nd.ID ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vernr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String tenNoiDung = rs.getString(2);
                    Object teamVal = rs.getObject(3);

                    boolean isTeam = parseTeamFlag(teamVal);
                    if (tenNoiDung != null && !tenNoiDung.isBlank()) {
                        (isTeam ? doubles : singles).put(tenNoiDung, id);
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Tải \"nội dung\" từ DB lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }

        return new Map[] { singles, doubles };
    }

    /** Chuyển giá trị TEAM về boolean */
    private boolean parseTeamFlag(Object v) {
        if (v == null)
            return false;
        if (v instanceof Boolean b)
            return b;
        if (v instanceof Number n)
            return n.intValue() != 0;
        String s = v.toString().trim();
        return s.equalsIgnoreCase("T")
                || s.equalsIgnoreCase("Y")
                || s.equalsIgnoreCase("TRUE")
                || s.equals("1");
    }
}
