package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.model.tournament.Vdv;

public class PlayerRepository {
    private final Connection conn;

    public PlayerRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Lấy VĐV đơn đã đăng ký trong sự kiện (su_kien_id) theo DB mới (MySQL).
     */
    public Map<String, Integer> loadSinglesNamesBySuKienId(int suKienId) {
        Map<String, Integer> nameToId = new LinkedHashMap<>();
        final String sql = "SELECT DISTINCT v.vdv_id AS VID, CONCAT(v.ho, ' ', v.ten) AS NM " +
                "FROM dangky dk " +
                "JOIN vdv v ON v.vdv_id = dk.vdv_id " +
                "WHERE dk.su_kien_id = ? AND dk.loai = 'DON' AND dk.trang_thai = 'DUYET' " +
                "ORDER BY NM";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, suKienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vid = rs.getInt("VID");
                    String nm = rs.getString("NM");
                    if (nm != null && !nm.isBlank()) {
                        nameToId.put(nm, vid);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tải VĐV (đơn) lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return nameToId;
    }

    /**
     * Tương thích ngược với tên cũ (KATNR) -> thực chất là su_kien_id.
     */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKatnr(int katnrAsSuKienId) {
        return loadSinglesNamesBySuKienId(katnrAsSuKienId);
    }

    /**
     * Tương thích ngược (KNR) -> thực chất là su_kien_id.
     */
    @Deprecated
    public Map<String, Integer> loadSinglesNamesByKnr(int knrAsSuKienId) {
        return loadSinglesNamesBySuKienId(knrAsSuKienId);
    }

    /**
     * Lấy danh sách VĐV
     */
    public List<Vdv> loadVdv() {
        List<Vdv> list = new ArrayList<>();
        if (conn == null) return list;

        final String sql = "SELECT vdv_id, ho, ten, gioi_tinh, ngay_sinh, clb_id, quoc_gia, sdt, email, xep_hang, created_at " +
                          "FROM vdv ORDER BY ho, ten";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vdv vdv = new Vdv();
                vdv.setVdvId(rs.getInt("vdv_id"));
                vdv.setHo(rs.getString("ho"));
                vdv.setTen(rs.getString("ten"));
                vdv.setGioiTinh(rs.getString("gioi_tinh"));
                if (rs.getDate("ngay_sinh") != null) {
                    vdv.setNgaySinh(rs.getDate("ngay_sinh").toLocalDate());
                }
                vdv.setClbId(rs.getObject("clb_id", Integer.class));
                vdv.setQuocGia(rs.getString("quoc_gia"));
                vdv.setSdt(rs.getString("sdt"));
                vdv.setEmail(rs.getString("email"));
                vdv.setXepHang(rs.getObject("xep_hang", Integer.class));
                vdv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(vdv);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tải danh sách VĐV lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /**
     * Tạo VĐV mới
     */
    public int createVdv(Vdv vdv) throws SQLException {
        final String sql = "INSERT INTO vdv (ho, ten, gioi_tinh, ngay_sinh, clb_id, quoc_gia, sdt, email, xep_hang) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vdv.getHo());
            ps.setString(2, vdv.getTen());
            ps.setString(3, vdv.getGioiTinh());
            ps.setDate(4, vdv.getNgaySinh() != null ? java.sql.Date.valueOf(vdv.getNgaySinh()) : null);
            ps.setObject(5, vdv.getClbId());
            ps.setString(6, vdv.getQuocGia());
            ps.setString(7, vdv.getSdt());
            ps.setString(8, vdv.getEmail());
            ps.setObject(9, vdv.getXepHang());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Cập nhật VĐV
     */
    public void updateVdv(Vdv vdv) throws SQLException {
        final String sql = "UPDATE vdv SET ho=?, ten=?, gioi_tinh=?, ngay_sinh=?, clb_id=?, quoc_gia=?, sdt=?, email=?, xep_hang=? WHERE vdv_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vdv.getHo());
            ps.setString(2, vdv.getTen());
            ps.setString(3, vdv.getGioiTinh());
            ps.setDate(4, vdv.getNgaySinh() != null ? java.sql.Date.valueOf(vdv.getNgaySinh()) : null);
            ps.setObject(5, vdv.getClbId());
            ps.setString(6, vdv.getQuocGia());
            ps.setString(7, vdv.getSdt());
            ps.setString(8, vdv.getEmail());
            ps.setObject(9, vdv.getXepHang());
            ps.setInt(10, vdv.getVdvId());
            ps.executeUpdate();
        }
    }

    /**
     * Xóa VĐV
     */
    public void deleteVdv(int vdvId) throws SQLException {
        final String sql = "DELETE FROM vdv WHERE vdv_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vdvId);
            ps.executeUpdate();
        }
    }
}
