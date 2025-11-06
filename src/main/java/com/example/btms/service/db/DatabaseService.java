// com/example/btms/service/DatabaseService.java
package com.example.btms.service.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.model.db.SQLSRVConnectionManager;
import com.example.btms.model.system.ServerInfo;
import com.example.btms.model.system.SettingsDetector;

@Service
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
