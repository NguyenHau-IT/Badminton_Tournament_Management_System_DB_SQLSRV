package com.example.btms.repository.club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.club.CauLacBo;

public class CauLacBoRepository {
    private final Connection conn;

    public CauLacBoRepository(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public CauLacBo add(CauLacBo clb) {
        String sql = "INSERT INTO CAU_LAC_BO (TEN_CLB, TEN_NGAN) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clb.getTenClb());
            ps.setString(2, clb.getTenNgan());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    clb.setId(rs.getInt(1));
            }
            return clb;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm câu lạc bộ", e);
        }
    }

    // READ - tất cả
    public List<CauLacBo> findAll() {
        List<CauLacBo> list = new ArrayList<>();
        String sql = "SELECT ID, TEN_CLB, TEN_NGAN FROM CAU_LAC_BO ORDER BY ID";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách câu lạc bộ", e);
        }
    }

    // READ - theo ID
    public CauLacBo findById(int id) {
        String sql = "SELECT ID, TEN_CLB, TEN_NGAN FROM CAU_LAC_BO WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm câu lạc bộ theo ID=" + id, e);
        }
    }

    // READ - theo tên đầy đủ
    public CauLacBo findByTen(String ten) {
        String sql = "SELECT ID, TEN_CLB, TEN_NGAN FROM CAU_LAC_BO WHERE UPPER(TEN_CLB) = UPPER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ten);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm câu lạc bộ theo tên=" + ten, e);
        }
    }

    // UPDATE
    public void update(CauLacBo clb) {
        String sql = "UPDATE CAU_LAC_BO SET TEN_CLB = ?, TEN_NGAN = ? WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clb.getTenClb());
            ps.setString(2, clb.getTenNgan());
            ps.setInt(3, clb.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật câu lạc bộ ID=" + clb.getId(), e);
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM CAU_LAC_BO WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xoá câu lạc bộ ID=" + id, e);
        }
    }

    // Mapper
    private CauLacBo map(ResultSet rs) throws SQLException {
        return new CauLacBo(
                rs.getInt("ID"),
                rs.getString("TEN_CLB"),
                rs.getString("TEN_NGAN"));
    }
}
