package com.example.badmintoneventtechnology.model.db;

import com.example.badmintoneventtechnology.config.ConnectionConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Quản lý kết nối SQL Server một cách mỏng, dựa vào ConnectionConfig. */
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

    /**
     * Ưu tiên dùng buildJdbcUrl() trong ConnectionConfig (đã parse & có defaults).
     */
    public String buildUrl() {
        if (cfg == null)
            throw new IllegalStateException("ConnectionConfig is null");
        return cfg.buildJdbcUrl();
    }

    public Connection connect() throws SQLException {
        close();
        if (cfg == null)
            throw new IllegalStateException("ConnectionConfig is null");
        String url = buildUrl();

        if (cfg.effectiveIntegratedSecurity()) {
            conn = DriverManager.getConnection(url);
        } else {
            conn = DriverManager.getConnection(url, cfg.user(), cfg.password());
        }
        return conn;
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignore) {
            } finally {
                conn = null;
            }
        }
    }
}
