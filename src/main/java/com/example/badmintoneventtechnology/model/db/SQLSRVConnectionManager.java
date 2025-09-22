package com.example.badmintoneventtechnology.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLSRVConnectionManager implements AutoCloseable {
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
        String port = cfg.port().trim();
        String dbName = cfg.databaseInput().trim();

        // Xử lý instance name với backslash (ví dụ: GODZILLA\SQLDEV)
        if (host.contains("\\")) {
            String[] parts = host.split("\\\\");
            String serverName = parts[0];
            String instanceName = parts[1];
            return "jdbc:sqlserver://" + serverName + ":" + port + ";instanceName=" + instanceName
                    + ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true;";
        } else {
            return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName
                    + ";encrypt=true;trustServerCertificate=true;";
        }
    }

    public Connection connect() throws SQLException {
        close();
        String url = buildUrl();
        String user = cfg.user();
        String password = cfg.password();
        conn = DriverManager.getConnection(url, user, password);
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