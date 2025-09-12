// com/example/mysqlclient/service/DatabaseService.java
package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.example.badmintoneventtechnology.model.db.ConnectionConfig;
import com.example.badmintoneventtechnology.model.db.MySqlConnectionManager;
import com.example.badmintoneventtechnology.model.system.ServerInfo;
import com.example.badmintoneventtechnology.model.system.SettingsDetector;

public class DatabaseService {
    private final MySqlConnectionManager manager;

    public DatabaseService(MySqlConnectionManager manager) {
        this.manager = manager;
    }

    public void setConfig(ConnectionConfig cfg) {
        manager.setConfig(cfg);
    }

    public MySqlConnectionManager manager() {
        return manager;
    }

    public Connection connect() throws SQLException {
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
