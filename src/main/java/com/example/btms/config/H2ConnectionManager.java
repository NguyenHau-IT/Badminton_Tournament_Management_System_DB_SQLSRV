package com.example.btms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class H2ConnectionManager implements AutoCloseable {
    private ConnectionH2Config cfg;
    private Connection conn;

    public void setConfig(ConnectionH2Config cfg) {
        this.cfg = cfg;
    }

    public Connection get() {
        return conn;
    }

    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public String buildUrl() {
        String host = cfg.host().trim();
        String port = cfg.port().trim();
        String raw = cfg.databaseInput().trim();
        String dbPath = switch (cfg.mode()) {
            case NAME -> raw.startsWith("./database/") ? raw : ("./database/" + raw);
            case HOME -> raw.startsWith("~/") ? raw : ("~/" + raw);
            case ABSOLUTE -> raw;
        };
        return "jdbc:h2:tcp://" + host + ":" + port + "/" + dbPath + ";IFEXISTS=TRUE";
    }

    public Connection connect() throws SQLException {
        close();
        conn = DriverManager.getConnection(buildUrl(), cfg.user(), cfg.password());
        return conn;
    }

    public record ProbeResult(Connection connection, String chosenUrl) {
    }

    public ProbeResult probeCommonPaths() throws SQLException {
        String host = cfg.host().trim();
        String port = cfg.port().trim();
        String name = cfg.databaseInput().trim();
        String user = cfg.user().trim();
        String pass = cfg.password();

        List<String> candidates = new ArrayList<>();
        candidates.add("jdbc:h2:tcp://" + host + ":" + port + "/~/" + name + ";IFEXISTS=TRUE");
        for (char d = 'C'; d <= 'Z'; d++) {
            candidates.add(
                    "jdbc:h2:tcp://" + host + ":" + port + "/" + d + ":/SET-OVR/database/" + name + ";IFEXISTS=TRUE");
        }

        for (String url : candidates) {
            Connection test = null;
            try {
                test = DriverManager.getConnection(url, user, pass);
                close(); // close previous if any
                conn = test; // hold open
                return new ProbeResult(conn, url);
            } catch (SQLException e) {
                if (test != null)
                    try {
                        test.close();
                    } catch (Exception ignore) {
                    }
            }
        }
        throw new SQLException("No candidate path worked. Please provide a correct ABSOLUTE PATH on the server.");
    }

    @Override
    public void close() {
        if (conn != null)
            try {
                conn.close();
            } catch (Exception ignore) {
            } finally {
                conn = null;
            }
    }
}