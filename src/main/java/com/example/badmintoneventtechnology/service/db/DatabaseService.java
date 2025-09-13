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

    // Quản lý giải đấu
    public java.util.List<com.example.badmintoneventtechnology.model.tournament.Giai> getAllGiai() {
        var list = new java.util.ArrayList<com.example.badmintoneventtechnology.model.tournament.Giai>();
        var conn = current();
        if (conn == null) {
            return list; // Trả về list rỗng nếu không có connection
        }
        try (var stmt = conn.prepareStatement("SELECT * FROM giai");
                var rs = stmt.executeQuery()) {
            while (rs.next()) {
                var g = new com.example.badmintoneventtechnology.model.tournament.Giai();
                g.setGiaiId(rs.getInt("giai_id"));
                g.setTen(rs.getString("ten"));
                g.setCapDo(rs.getString("cap_do"));
                g.setDiaDiem(rs.getString("dia_diem"));
                g.setThanhPho(rs.getString("thanh_pho"));
                g.setNgayBd(rs.getDate("ngay_bd").toLocalDate());
                g.setNgayKt(rs.getDate("ngay_kt").toLocalDate());
                list.add(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addGiai(com.example.badmintoneventtechnology.model.tournament.Giai g) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement(
                "INSERT INTO giai (ten, cap_do, dia_diem, thanh_pho, ngay_bd, ngay_kt) VALUES (?, ?, ?, ?, ?, ?)");) {
            stmt.setString(1, g.getTen());
            stmt.setString(2, g.getCapDo());
            stmt.setString(3, g.getDiaDiem());
            stmt.setString(4, g.getThanhPho());
            stmt.setDate(5, java.sql.Date.valueOf(g.getNgayBd()));
            stmt.setDate(6, java.sql.Date.valueOf(g.getNgayKt()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public com.example.badmintoneventtechnology.model.tournament.Giai getGiaiById(int id) {
        var conn = current();
        if (conn == null)
            return null;
        try (var stmt = conn.prepareStatement("SELECT * FROM giai WHERE giai_id = ?");) {
            stmt.setInt(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var g = new com.example.badmintoneventtechnology.model.tournament.Giai();
                    g.setGiaiId(rs.getInt("giai_id"));
                    g.setTen(rs.getString("ten"));
                    g.setCapDo(rs.getString("cap_do"));
                    g.setDiaDiem(rs.getString("dia_diem"));
                    g.setThanhPho(rs.getString("thanh_pho"));
                    g.setNgayBd(rs.getDate("ngay_bd").toLocalDate());
                    g.setNgayKt(rs.getDate("ngay_kt").toLocalDate());
                    return g;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateGiai(com.example.badmintoneventtechnology.model.tournament.Giai g) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement(
                "UPDATE giai SET ten=?, cap_do=?, dia_diem=?, thanh_pho=?, ngay_bd=?, ngay_kt=? WHERE giai_id=?");) {
            stmt.setString(1, g.getTen());
            stmt.setString(2, g.getCapDo());
            stmt.setString(3, g.getDiaDiem());
            stmt.setString(4, g.getThanhPho());
            stmt.setDate(5, java.sql.Date.valueOf(g.getNgayBd()));
            stmt.setDate(6, java.sql.Date.valueOf(g.getNgayKt()));
            stmt.setInt(7, g.getGiaiId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGiai(int id) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement("DELETE FROM giai WHERE giai_id=?");) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
