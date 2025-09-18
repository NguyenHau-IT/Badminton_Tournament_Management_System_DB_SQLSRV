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

    // Quản lý sự kiện
    public java.util.List<com.example.badmintoneventtechnology.model.tournament.SuKien> getSuKienByGiai(int giaiId) {
        var list = new java.util.ArrayList<com.example.badmintoneventtechnology.model.tournament.SuKien>();
        var conn = current();
        if (conn == null)
            return list;
        try (var stmt = conn.prepareStatement("SELECT * FROM su_kien WHERE giai_id = ? ORDER BY su_kien_id")) {
            stmt.setInt(1, giaiId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var s = new com.example.badmintoneventtechnology.model.tournament.SuKien();
                    s.setSuKienId(rs.getInt("su_kien_id"));
                    s.setGiaiId(rs.getInt("giai_id"));
                    s.setMa(rs.getString("ma"));
                    s.setTen(rs.getString("ten"));
                    s.setNhomTuoi(rs.getString("nhom_tuoi"));
                    s.setTrinhDo(rs.getString("trinh_do"));
                    s.setSoLuong(rs.getInt("so_luong"));
                    s.setLuatThiDau(rs.getString("luat_thi_dau"));
                    s.setLoaiBang(rs.getString("loai_bang"));
                    s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addSuKien(com.example.badmintoneventtechnology.model.tournament.SuKien s) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement(
                "INSERT INTO su_kien (giai_id, ma, ten, nhom_tuoi, trinh_do, so_luong, luat_thi_dau, loai_bang) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");) {
            stmt.setInt(1, s.getGiaiId());
            stmt.setString(2, s.getMa());
            stmt.setString(3, s.getTen());
            stmt.setString(4, s.getNhomTuoi());
            stmt.setString(5, s.getTrinhDo());
            stmt.setInt(6, s.getSoLuong());
            stmt.setString(7, s.getLuatThiDau());
            stmt.setString(8, s.getLoaiBang());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Lỗi thêm sự kiện: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi không xác định khi thêm sự kiện", e);
        }
    }

    public void updateSuKien(com.example.badmintoneventtechnology.model.tournament.SuKien s) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement(
                "UPDATE su_kien SET ma=?, ten=?, nhom_tuoi=?, trinh_do=?, so_luong=?, luat_thi_dau=?, loai_bang=? WHERE su_kien_id=?");) {
            stmt.setString(1, s.getMa());
            stmt.setString(2, s.getTen());
            stmt.setString(3, s.getNhomTuoi());
            stmt.setString(4, s.getTrinhDo());
            stmt.setInt(5, s.getSoLuong());
            stmt.setString(6, s.getLuatThiDau());
            stmt.setString(7, s.getLoaiBang());
            stmt.setInt(8, s.getSuKienId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSuKien(int suKienId) {
        var conn = current();
        if (conn == null)
            return;
        try (var stmt = conn.prepareStatement("DELETE FROM su_kien WHERE su_kien_id=?");) {
            stmt.setInt(1, suKienId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
