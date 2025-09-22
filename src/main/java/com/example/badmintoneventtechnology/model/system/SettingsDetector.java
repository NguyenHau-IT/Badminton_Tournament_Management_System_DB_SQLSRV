package com.example.badmintoneventtechnology.model.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SettingsDetector {
    public static ServerInfo detect(Connection c) {
        String baseDir = fetchSetting(c, "BASE_DIR");
        String dbPath = databasePath(c);
        if ((baseDir == null || baseDir.isBlank()) && dbPath != null) {
            baseDir = guessBaseDirFromDbPath(dbPath);
        }
        String dbName = (dbPath == null) ? "-" : extractDbName(dbPath);
        return new ServerInfo(baseDir == null ? "(not set)" : baseDir, dbPath == null ? "-" : dbPath, dbName);
    }

    private static String fetchSetting(Connection c, String name) {
        String sql = "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString(1);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static String databasePath(Connection c) {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT DATABASE_PATH()")) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static String guessBaseDirFromDbPath(String dbPath) {
        if (dbPath == null)
            return null;
        String p = dbPath.replace("\\", "/");
        int slash = p.lastIndexOf('/');
        if (slash < 0)
            return null;
        return p.substring(0, slash);
    }

    private static String extractDbName(String dbPath) {
        String p = dbPath.replace("\\", "/");
        int slash = p.lastIndexOf('/');
        String file = (slash >= 0) ? p.substring(slash + 1) : p;
        String f = file.toLowerCase();
        if (f.endsWith(".mv.db"))
            return file.substring(0, file.length() - ".mv.db".length());
        if (f.endsWith(".h2.db"))
            return file.substring(0, file.length() - ".h2.db".length());
        return file;
    }
}
