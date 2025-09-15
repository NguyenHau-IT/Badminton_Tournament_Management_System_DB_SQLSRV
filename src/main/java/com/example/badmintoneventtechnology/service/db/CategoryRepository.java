package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;

public class CategoryRepository {
    private final Connection conn;

    public CategoryRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Trả về mảng 2 phần tử: [singles, doubles]
     * - singles: map nội dung đơn (tenSuKien -> suKienId)
     * - doubles: map nội dung đôi/đồng đội (tenSuKien -> suKienId)
     */
    public Map<String, Integer>[] loadCategories() {
        return loadCategories(null);
    }

    /**
     * Trả về mảng 2 phần tử: [singles, doubles] theo giải đấu được chọn
     * - singles: map nội dung đơn (tenSuKien -> suKienId)
     * - doubles: map nội dung đôi/đồng đội (tenSuKien -> suKienId)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer>[] loadCategories(Integer giaiId) {
        Map<String, Integer> singles = new LinkedHashMap<>();
        Map<String, Integer> doubles = new LinkedHashMap<>();

        if (conn == null) {
            return (Map<String, Integer>[]) new Map[] { singles, doubles };
        }

        // Nếu có giaiId, lấy su_kien theo giải đấu, nếu không thì lấy tất cả
        String sql;
        if (giaiId != null) {
            sql = "SELECT su_kien_id, ten, ma " +
                    "FROM su_kien " +
                    "WHERE giai_id = " + giaiId + " " +
                    "ORDER BY ten";
        } else {
            sql = "SELECT su_kien_id, ten, ma FROM su_kien ORDER BY ten";
        }

        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int suKienId = rs.getInt("su_kien_id");
                String tenSuKien = rs.getString("ten");
                String ma = rs.getString("ma");

                // Chuẩn hoá & kiểm tra null
                String maNorm = ma == null ? "" : ma.trim().toLowerCase(Locale.ROOT);

                // Xác định nội dung đôi/đồng đội
                boolean isTeam = maNorm.equals("donam") // Đôi nam
                        || maNorm.equals("donu") // Đôi nữ
                        || maNorm.equals("donamnu"); // Đôi nam nữ

                if (tenSuKien != null && !tenSuKien.isBlank()) {
                    (isTeam ? doubles : singles).put(tenSuKien, suKienId);
                }
            }

        } catch (SQLException ex) {
            String errorMsg = giaiId != null
                    ? "Tải \"nội dung\" cho giải đấu từ DB lỗi: " + ex.getMessage()
                    : "Tải \"nội dung\" từ DB lỗi: " + ex.getMessage();
            JOptionPane.showMessageDialog(
                    null,
                    errorMsg,
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }

        return (Map<String, Integer>[]) new Map[] { singles, doubles };
    }
}
