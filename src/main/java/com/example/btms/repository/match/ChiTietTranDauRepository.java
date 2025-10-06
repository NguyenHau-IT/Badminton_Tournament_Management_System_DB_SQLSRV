package com.example.btms.repository.match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.match.ChiTietTranDau;

public class ChiTietTranDauRepository {
    private final Connection conn;

    public ChiTietTranDauRepository(Connection conn) {
        this.conn = conn;
    }

    public void add(ChiTietTranDau m) {
        final String sql = "INSERT INTO CHI_TIET_TRAN_DAU " +
                "(ID, THE_THUC, ID_VDV_THANG, BAT_DAU, KET_THUC, SAN) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setInt(2, m.getTheThuc());
            ps.setInt(3, m.getIdVdvThang());
            ps.setTimestamp(4, Timestamp.valueOf(m.getBatDau()));
            ps.setTimestamp(5, Timestamp.valueOf(m.getKetThuc()));
            ps.setInt(6, m.getSan());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm CHI_TIET_TRAN_DAU", e);
        }
    }

    public ChiTietTranDau findById(String id) {
        final String sql = "SELECT * FROM CHI_TIET_TRAN_DAU WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm CHI_TIET_TRAN_DAU", e);
        }
    }

    public List<ChiTietTranDau> listAll() {
        final String sql = "SELECT * FROM CHI_TIET_TRAN_DAU ORDER BY BAT_DAU";
        List<ChiTietTranDau> out = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                out.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi list CHI_TIET_TRAN_DAU", e);
        }
        return out;
    }

    public void update(ChiTietTranDau m) {
        final String sql = "UPDATE CHI_TIET_TRAN_DAU SET THE_THUC=?, ID_VDV_THANG=?, " +
                "BAT_DAU=?, KET_THUC=?, SAN=? WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getTheThuc());
            ps.setInt(2, m.getIdVdvThang());
            ps.setTimestamp(3, Timestamp.valueOf(m.getBatDau()));
            ps.setTimestamp(4, Timestamp.valueOf(m.getKetThuc()));
            ps.setInt(5, m.getSan());
            ps.setString(6, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update CHI_TIET_TRAN_DAU", e);
        }
    }

    public void delete(String id) {
        final String sql = "DELETE FROM CHI_TIET_TRAN_DAU WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi delete CHI_TIET_TRAN_DAU", e);
        }
    }

    private ChiTietTranDau map(ResultSet rs) throws SQLException {
        return new ChiTietTranDau(
                rs.getString("ID"),
                rs.getInt("THE_THUC"),
                rs.getInt("ID_VDV_THANG"),
                rs.getTimestamp("BAT_DAU").toLocalDateTime(),
                rs.getTimestamp("KET_THUC").toLocalDateTime(),
                rs.getInt("SAN"));
    }
}
