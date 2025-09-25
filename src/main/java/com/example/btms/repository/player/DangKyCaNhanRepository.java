package com.example.btms.repository.player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.player.DangKyCaNhan;

/**
 * JDBC Repository cho bảng DANG_KI_CA_NHAN.
 * Cột giả định: ID (identity), ID_GIAI, ID_NOI_DUNG, ID_VDV
 */
public class DangKyCaNhanRepository {
    private final Connection conn;

    public DangKyCaNhanRepository(Connection conn) { this.conn = conn; }

    public DangKyCaNhan add(DangKyCaNhan d) {
        final String sql = "INSERT INTO DANG_KI_CA_NHAN (ID_GIAI, ID_NOI_DUNG, ID_VDV) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getIdGiai());
            ps.setInt(2, d.getIdNoiDung());
            ps.setInt(3, d.getIdVdv());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setId(rs.getInt(1));
            }
            return d;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đăng ký cá nhân", e);
        }
    }

    public DangKyCaNhan findById(int id) {
        final String sql = "SELECT ID, ID_GIAI, ID_NOI_DUNG, ID_VDV FROM DANG_KI_CA_NHAN WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm đăng ký cá nhân id=" + id, e);
        }
    }

    public List<DangKyCaNhan> findAllByGiai(int idGiai) {
        final String sql = "SELECT ID, ID_GIAI, ID_NOI_DUNG, ID_VDV FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? ORDER BY ID";
        List<DangKyCaNhan> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đăng ký cá nhân theo giải", e);
        }
        return list;
    }

    public List<DangKyCaNhan> findAllByGiaiAndNoiDung(int idGiai, int idNoiDung) {
        final String sql = "SELECT ID, ID_GIAI, ID_NOI_DUNG, ID_VDV FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY ID";
        List<DangKyCaNhan> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải đăng ký cá nhân theo giải & nội dung", e);
        }
        return list;
    }

    public void update(DangKyCaNhan d) {
        final String sql = "UPDATE DANG_KI_CA_NHAN SET ID_GIAI=?, ID_NOI_DUNG=?, ID_VDV=? WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getIdGiai());
            ps.setInt(2, d.getIdNoiDung());
            ps.setInt(3, d.getIdVdv());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật đăng ký cá nhân id=" + d.getId(), e);
        }
    }

    public void delete(int id) {
        final String sql = "DELETE FROM DANG_KI_CA_NHAN WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xoá đăng ký cá nhân id=" + id, e);
        }
    }

    public boolean exists(int idGiai, int idNoiDung, int idVdv) {
        final String sql = "SELECT 1 FROM DANG_KI_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra tồn tại đăng ký cá nhân", e);
        }
    }

    private DangKyCaNhan map(ResultSet rs) throws SQLException {
        return new DangKyCaNhan(
                rs.getInt("ID"),
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"));
    }
}
