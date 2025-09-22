// com/example/badmintoneventtechnology/service/DatabaseService.java
package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.example.badmintoneventtechnology.model.db.ConnectionConfig;
import com.example.badmintoneventtechnology.model.db.SQLSRVConnectionManager;
import com.example.badmintoneventtechnology.model.system.ServerInfo;
import com.example.badmintoneventtechnology.model.system.SettingsDetector;

public class DatabaseService {
    private final SQLSRVConnectionManager manager;

    public DatabaseService(SQLSRVConnectionManager manager) {
        this.manager = manager;
    }

    // ✅ thêm 2 method này
    public void setConfig(ConnectionConfig cfg) {
        manager.setConfig(cfg);
    }

    public SQLSRVConnectionManager manager() {
        return manager;
    } // optional, nếu vẫn muốn dùng ở chỗ khác

    public Connection connect() throws SQLException {
        return manager.connect();
    }

    public Connection probeAndConnect() throws SQLException {
        // For SQL Server, we just try to connect directly
        return manager.connect();
    }

    public void disconnect() {
        manager.close();
    }

    public boolean isConnected() {
        return manager.isConnected();
    }

    public String builtUrl() {
        return manager.buildUrl();
    }

    public ServerInfo detectInfo(Connection c) {
        return SettingsDetector.detect(c);
    }

    public Connection current() {
        return manager.get();
    }
}
