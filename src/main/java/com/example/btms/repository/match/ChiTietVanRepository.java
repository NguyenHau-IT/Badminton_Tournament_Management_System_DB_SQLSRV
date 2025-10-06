package com.example.btms.repository.match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.match.ChiTietVan;

public class ChiTietVanRepository {
    private final Connection conn;

    public ChiTietVanRepository(Connection conn) {
        this.conn = conn;
    }

    public void add(ChiTietVan s) {
        final String sql = "INSERT INTO CHI_TIET_VAN (ID_TRAN_DAU, SET_NO, TONG_DIEM_1, TONG_DIEM_2, DAU_THOI_GIAN) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getIdTranDau());
            ps.setInt(2, s.getSetNo());
            ps.setInt(3, s.getTongDiem1());
            ps.setInt(4, s.getTongDiem2());
            ps.setString(5, s.getDauThoiGian());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm CHI_TIET_VAN", e);
        }
    }

    public ChiTietVan findOne(String idTranDau, int setNo) {
        final String sql = "SELECT * FROM CHI_TIET_VAN WHERE ID_TRAN_DAU=? AND SET_NO=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idTranDau);
            ps.setInt(2, setNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm CHI_TIET_VAN", e);
        }
    }

    public List<ChiTietVan> listByMatch(String idTranDau) {
        final String sql = "SELECT * FROM CHI_TIET_VAN WHERE ID_TRAN_DAU=? ORDER BY SET_NO";
        List<ChiTietVan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idTranDau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi list CHI_TIET_VAN", e);
        }
        return out;
    }

    public void update(ChiTietVan s) {
        final String sql = "UPDATE CHI_TIET_VAN SET TONG_DIEM_1=?, TONG_DIEM_2=?, DAU_THOI_GIAN=? " +
                "WHERE ID_TRAN_DAU=? AND SET_NO=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getTongDiem1());
            ps.setInt(2, s.getTongDiem2());
            ps.setString(3, s.getDauThoiGian());
            ps.setString(4, s.getIdTranDau());
            ps.setInt(5, s.getSetNo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update CHI_TIET_VAN", e);
        }
    }

    public void delete(String idTranDau, int setNo) {
        final String sql = "DELETE FROM CHI_TIET_VAN WHERE ID_TRAN_DAU=? AND SET_NO=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idTranDau);
            ps.setInt(2, setNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi delete CHI_TIET_VAN", e);
        }
    }

    private ChiTietVan map(ResultSet rs) throws SQLException {
        return new ChiTietVan(
                rs.getString("ID_TRAN_DAU"),
                rs.getInt("SET_NO"),
                rs.getInt("TONG_DIEM_1"),
                rs.getInt("TONG_DIEM_2"),
                rs.getString("DAU_THOI_GIAN"));
    }
}
