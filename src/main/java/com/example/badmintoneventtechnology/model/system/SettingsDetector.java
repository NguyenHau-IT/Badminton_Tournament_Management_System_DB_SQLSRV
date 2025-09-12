package com.example.badmintoneventtechnology.model.system;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SettingsDetector {
    public static ServerInfo detect(Connection c) {
        String dbName = currentDatabase(c);
        String dataDir = dataDirectory(c);
        return new ServerInfo(dataDir == null ? "(unknown)" : dataDir, "-", dbName == null ? "-" : dbName);
    }

    private static String currentDatabase(Connection c) {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static String dataDirectory(Connection c) {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT @@datadir")) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException ignored) {
        }
        return null;
    }
}
