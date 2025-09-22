package com.example.badmintoneventtechnology.service.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.badmintoneventtechnology.model.auth.AuthResult;

public class AuthService {
    private final Connection conn;

    public AuthService(Connection conn) {
        this.conn = conn;
    }

    /** Trả về (found, locked) theo bảng PUBLIC."USER" */
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
                    return new AuthResult(false, false);
                boolean locked = toLocked(rs.getObject("LOCKED"));
                return new AuthResult(true, locked);
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
