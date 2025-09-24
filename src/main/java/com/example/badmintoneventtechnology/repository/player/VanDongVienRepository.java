package com.example.badmintoneventtechnology.repository.player;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.model.player.VanDongVien;

public class VanDongVienRepository {
    private final Connection conn;

    public VanDongVienRepository(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public VanDongVien add(VanDongVien vdv) {
        String sql = "INSERT INTO VAN_DONG_VIEN (HO_TEN, NGAY_SINH, ID_CLB, GIOI_TINH) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vdv.getHoTen());
            ps.setDate(2, vdv.getNgaySinh() != null ? Date.valueOf(vdv.getNgaySinh()) : null);
            if (vdv.getIdClb() != null)
                ps.setInt(3, vdv.getIdClb());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setString(4, vdv.getGioiTinh());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    vdv.setId(rs.getInt(1));
            }
            return vdv;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm vận động viên", e);
        }
    }

    // READ all
    public List<VanDongVien> findAll() {
        List<VanDongVien> list = new ArrayList<>();
        String sql = "SELECT * FROM VAN_DONG_VIEN ORDER BY ID";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách vận động viên", e);
        }
        return list;
    }

    // READ by ID
    public VanDongVien findById(int id) {
        String sql = "SELECT * FROM VAN_DONG_VIEN WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm vận động viên id=" + id, e);
        }
    }

    // UPDATE
    public void update(VanDongVien vdv) {
        String sql = "UPDATE VAN_DONG_VIEN SET HO_TEN=?, NGAY_SINH=?, ID_CLB=?, GIOI_TINH=? WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vdv.getHoTen());
            ps.setDate(2, vdv.getNgaySinh() != null ? Date.valueOf(vdv.getNgaySinh()) : null);
            if (vdv.getIdClb() != null)
                ps.setInt(3, vdv.getIdClb());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setString(4, vdv.getGioiTinh());
            ps.setInt(5, vdv.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật vận động viên id=" + vdv.getId(), e);
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM VAN_DONG_VIEN WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa vận động viên id=" + id, e);
        }
    }

    private VanDongVien map(ResultSet rs) throws SQLException {
        return new VanDongVien(
                rs.getInt("ID"),
                rs.getString("HO_TEN"),
                rs.getDate("NGAY_SINH") != null ? rs.getDate("NGAY_SINH").toLocalDate() : null,
                rs.getObject("ID_CLB") != null ? rs.getInt("ID_CLB") : null,
                rs.getString("GIOI_TINH"));
    }

    public Map<String, Integer> loadSinglesNames(int idNoiDung, int idGiaiDau) {
        Map<String, Integer> nameToId = new LinkedHashMap<>();
        final String sql = "SELECT DISTINCT v.ID, COALESCE(v.HO_TEN, CAST(d.ID_VDV AS VARCHAR)) AS TEN " +
                "FROM DANG_KI_CA_NHAN d " +
                "LEFT JOIN VAN_DONG_VIEN v ON v.ID = d.ID_VDV " +
                "WHERE d.ID_NOI_DUNG = ? AND d.ID_GIAI = ? " +
                "ORDER BY TEN";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNoiDung);
            ps.setInt(2, idGiaiDau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vdvId = rs.getInt("ID");
                    String ten = rs.getString("TEN");
                    if (ten != null && !ten.isBlank()) {
                        nameToId.put(ten, vdvId);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Lỗi tải VĐV đăng ký cá nhân: " + ex.getMessage(),
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }
        return nameToId;
    }
}
