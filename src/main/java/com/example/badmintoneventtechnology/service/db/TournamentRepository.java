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

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.model.tournament.SuKien;

public class TournamentRepository {
    private final Connection conn;

    public TournamentRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Lấy danh sách giải đấu
     */
    public List<Giai> loadGiai() {
        List<Giai> list = new ArrayList<>();
        if (conn == null)
            return list;

        final String sql = "SELECT giai_id, ten, cap_do, dia_diem, thanh_pho, ngay_bd, ngay_kt, created_at FROM giai ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Giai giai = new Giai();
                giai.setGiaiId(rs.getInt("giai_id"));
                giai.setTen(rs.getString("ten"));
                giai.setCapDo(rs.getString("cap_do"));
                giai.setDiaDiem(rs.getString("dia_diem"));
                giai.setThanhPho(rs.getString("thanh_pho"));
                giai.setNgayBd(rs.getDate("ngay_bd").toLocalDate());
                giai.setNgayKt(rs.getDate("ngay_kt").toLocalDate());
                giai.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(giai);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tải danh sách giải đấu lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /**
     * Lấy danh sách sự kiện theo giải đấu
     */
    public List<SuKien> loadSuKienByGiai(int giaiId) {
        List<SuKien> list = new ArrayList<>();
        if (conn == null)
            return list;

        final String sql = "SELECT su_kien_id, giai_id, ma, ten, nhom_tuoi, trinh_do, so_luong, luat_thi_dau, loai_bang, created_at "
                +
                "FROM su_kien WHERE giai_id = ? ORDER BY ma, ten";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giaiId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SuKien suKien = new SuKien();
                    suKien.setSuKienId(rs.getInt("su_kien_id"));
                    suKien.setGiaiId(rs.getInt("giai_id"));
                    suKien.setMa(rs.getString("ma"));
                    suKien.setTen(rs.getString("ten"));
                    suKien.setNhomTuoi(rs.getString("nhom_tuoi"));
                    suKien.setTrinhDo(rs.getString("trinh_do"));
                    suKien.setSoLuong(rs.getInt("so_luong"));
                    suKien.setLuatThiDau(rs.getString("luat_thi_dau"));
                    suKien.setLoaiBang(rs.getString("loai_bang"));
                    suKien.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(suKien);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Tải danh sách sự kiện lỗi: " + ex.getMessage(),
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /**
     * Trả về mảng 2 phần tử: [singles, doubles] - tương thích với
     * CategoryRepository cũ
     * - singles: map nội dung đơn (tenSuKien -> suKienId)
     * - doubles: map nội dung đôi/đồng đội (tenSuKien -> suKienId)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer>[] loadCategories() {
        Map<String, Integer> singles = new LinkedHashMap<>();
        Map<String, Integer> doubles = new LinkedHashMap<>();

        if (conn == null) {
            return (Map<String, Integer>[]) new Map[] { singles, doubles };
        }

        final String sql = "SELECT su_kien_id, ten, ma FROM su_kien ORDER BY ten";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int suKienId = rs.getInt("su_kien_id");
                String tenSuKien = rs.getString("ten");
                String ma = rs.getString("ma");

                // Chuẩn hoá & kiểm tra null
                String maNorm = ma == null ? "" : ma.trim().toLowerCase();

                // Xác định nội dung đôi/đồng đội
                boolean isTeam = maNorm.equals("donam") // Đôi nam
                        || maNorm.equals("donu") // Đôi nữ
                        || maNorm.equals("donamnu"); // Đôi nam nữ

                if (tenSuKien != null && !tenSuKien.isBlank()) {
                    (isTeam ? doubles : singles).put(tenSuKien, suKienId);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Tải \"nội dung\" từ DB lỗi: " + ex.getMessage(),
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }

        return (Map<String, Integer>[]) new Map[] { singles, doubles };
    }

    /**
     * Tạo giải đấu mới
     */
    public int createGiai(Giai giai) throws SQLException {
        final String sql = "INSERT INTO giai (ten, cap_do, dia_diem, thanh_pho, ngay_bd, ngay_kt) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, giai.getTen());
            ps.setString(2, giai.getCapDo());
            ps.setString(3, giai.getDiaDiem());
            ps.setString(4, giai.getThanhPho());
            ps.setDate(5, java.sql.Date.valueOf(giai.getNgayBd()));
            ps.setDate(6, java.sql.Date.valueOf(giai.getNgayKt()));
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
     * Tạo sự kiện mới
     */
    public int createSuKien(SuKien suKien) throws SQLException {
        final String sql = "INSERT INTO su_kien (giai_id, ma, ten, nhom_tuoi, trinh_do, so_luong, luat_thi_dau, loai_bang) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, suKien.getGiaiId());
            ps.setString(2, suKien.getMa());
            ps.setString(3, suKien.getTen());
            ps.setString(4, suKien.getNhomTuoi());
            ps.setString(5, suKien.getTrinhDo());
            ps.setInt(6, suKien.getSoLuong());
            ps.setString(7, suKien.getLuatThiDau());
            ps.setString(8, suKien.getLoaiBang());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
}
