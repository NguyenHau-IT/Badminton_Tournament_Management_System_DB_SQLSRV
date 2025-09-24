package com.example.btms.service.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.btms.config.Prefs;
import com.example.btms.model.auth.AuthResult;

public class AuthService {
    private final Connection conn;

    public AuthService(Connection conn) {
        this.conn = conn;
    }

    /** Trả về (found, locked, userId) theo bảng NGUOI_DUNG */
    public AuthResult authenticate(String username, String md5Hex) throws Exception {
        String sql = """
                SELECT COALESCE(0, 0) AS LOCKED, ID, HO_TEN
                FROM NGUOI_DUNG
                WHERE UPPER(HO_TEN) = UPPER(?)
                  AND UPPER(MAT_KHAU) = UPPER(?);
                                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, md5Hex);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return new AuthResult(false, false, -1);
                boolean locked = toLocked(rs.getObject("LOCKED"));
                int userId = rs.getInt("ID");
                // Lưu userId vào Preferences
                Prefs prefs = new Prefs();
                prefs.putInt("userId", userId);
                return new AuthResult(true, locked, userId);
            }
        }
    }

    private static boolean toLocked(Object o) {
        if (o == null)
            return false;
        if (o instanceof Boolean b)
            return b;
        if (o instanceof Number n)
            return n.intValue() != 0;
        String s = o.toString();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }
}
