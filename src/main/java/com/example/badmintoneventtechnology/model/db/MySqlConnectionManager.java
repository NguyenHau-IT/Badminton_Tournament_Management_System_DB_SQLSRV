package com.example.badmintoneventtechnology.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlConnectionManager implements AutoCloseable {
    private ConnectionConfig cfg;
    private Connection conn;

    public void setConfig(ConnectionConfig cfg) {
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
        String port = (cfg.port() == null || cfg.port().isBlank()) ? "3306" : cfg.port().trim();
        String db = cfg.databaseInput().trim();
        // MySQL uses database name only; no path semantics
        return "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public Connection connect() throws SQLException {
        close();
        conn = DriverManager.getConnection(buildUrl(), cfg.user(), cfg.password());
        return conn;
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


