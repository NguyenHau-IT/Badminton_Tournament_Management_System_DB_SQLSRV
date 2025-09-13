package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class GiaiRelatedService {
    private final DatabaseService db;

    public GiaiRelatedService(DatabaseService db) {
        this.db = db;
    }

    public List<String> getNoiDungThiDauByGiai(int giaiId) {
        List<String> result = new ArrayList<>();
        Connection conn = db.current();
        if (conn == null)
            return result;

        try (var stmt = conn.prepareStatement("SELECT sk.ten FROM su_kien sk WHERE sk.giai_id = ?")) {
            stmt.setInt(1, giaiId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("ten"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getVdvByGiai(int giaiId) {
        List<String> result = new ArrayList<>();
        Connection conn = db.current();
        if (conn == null)
            return result;

        try (var stmt = conn.prepareStatement(
                "SELECT CONCAT(v.ho, ' ', v.ten) as ten_vdv FROM vdv v " +
                        "JOIN dangky dk ON v.vdv_id = dk.vdv_id " +
                        "JOIN su_kien sk ON dk.su_kien_id = sk.su_kien_id " +
                        "WHERE sk.giai_id = ?")) {
            stmt.setInt(1, giaiId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("ten_vdv"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
