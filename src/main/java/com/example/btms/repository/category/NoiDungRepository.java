package com.example.btms.repository.category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JOptionPane;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;

public class NoiDungRepository {
    // Đồng bộ tên biến với các repository khác (thường dùng 'conn')
    private final Connection conn;

    public NoiDungRepository(Connection connection) {
        this.conn = connection;
    }

    public NoiDung create(NoiDung noiDung) throws SQLException {
        String sql = "INSERT INTO NOI_DUNG (TEN_NOI_DUNG, TUOI_DUOI, TUOI_TREN, GIOI_TINH, TEAM) VALUES (?, ?, ?, ?, ?)";
        if (noiDung == null)
            throw new IllegalArgumentException("NoiDung null");
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, noiDung.getTenNoiDung());
            pstmt.setInt(2, noiDung.getTuoiDuoi());
            pstmt.setInt(3, noiDung.getTuoiTren());
            pstmt.setString(4, noiDung.getGioiTinh());
            pstmt.setBoolean(5, noiDung.getTeam());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating NoiDung failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    noiDung.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating NoiDung failed, no ID obtained.");
                }
            }
        }
        return noiDung;
    }

    public List<NoiDung> findAll() throws SQLException {
        List<NoiDung> list = new ArrayList<>();
        String sql = "SELECT * FROM NOI_DUNG ORDER BY ID";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNoiDung(rs));
            }
        }
        return list;
    }

    public List<NoiDung> findByTournament(Integer idGiai) throws SQLException {
        List<NoiDung> list = new ArrayList<>();
        // Nếu chưa chọn giải, trả về rỗng để tránh lỗi tải sớm
        if (idGiai == null || idGiai <= 0)
            return list;

        // Ưu tiên dùng cột ID_GIAI_DAU; nếu không tồn tại (schema khác), fallback sang
        // ID_GIAI
        String sql1 = "SELECT nd.ID, nd.TEN_NOI_DUNG, nd.TUOI_DUOI, nd.TUOI_TREN, nd.GIOI_TINH, nd.TEAM " +
                "FROM NOI_DUNG nd " +
                "JOIN CHI_TIET_GIAI_DAU ctgd ON ctgd.ID_NOI_DUNG = nd.ID " +
                "WHERE ctgd.ID_GIAI = ?";
        String sql2 = "SELECT nd.ID, nd.TEN_NOI_DUNG, nd.TUOI_DUOI, nd.TUOI_TREN, nd.GIOI_TINH, nd.TEAM " +
                "FROM NOI_DUNG nd " +
                "JOIN CHI_TIET_GIAI_DAU ctgd ON ctgd.ID_NOI_DUNG = nd.ID " +
                "WHERE ctgd.ID_GIAI = ?";

        SQLException firstEx = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setInt(1, idGiai);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSetToNoiDung(rs));
            }
            return list;
        } catch (SQLException e1) {
            firstEx = e1;
            // thử fallback với cột ID_GIAI
            list.clear();
            try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                pstmt.setInt(1, idGiai);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next())
                        list.add(mapResultSetToNoiDung(rs));
                }
                return list;
            } catch (SQLException e2) {
                // ném lại lỗi đầu tiên để giữ nguyên ngữ cảnh
                throw firstEx;
            }
        }
    }

    public Optional<NoiDung> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM NOI_DUNG WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNoiDung(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean update(NoiDung noiDung) throws SQLException {
        String sql = "UPDATE NOI_DUNG SET TEN_NOI_DUNG = ?, TUOI_DUOI = ?, TUOI_TREN = ?, GIOI_TINH = ?, TEAM = ? WHERE ID = ?";
        if (noiDung == null || noiDung.getId() == null)
            throw new IllegalArgumentException("NoiDung hoặc ID null");
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, noiDung.getTenNoiDung());
            pstmt.setInt(2, noiDung.getTuoiDuoi());
            pstmt.setInt(3, noiDung.getTuoiTren());
            pstmt.setString(4, noiDung.getGioiTinh());
            pstmt.setBoolean(5, noiDung.getTeam());
            pstmt.setInt(6, noiDung.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM NOI_DUNG WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean isNoiDungIsTeam(Integer idNoiDung) throws SQLException {
        String sql = "SELECT TEAM FROM NOI_DUNG WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idNoiDung);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("TEAM"); // BIT(1) → boolean
                }
            }
        }
        return false; // nếu không tìm thấy ID hoặc null → coi như false
    }

    private NoiDung mapResultSetToNoiDung(ResultSet rs) throws SQLException {
        NoiDung nd = new NoiDung();
        nd.setId(rs.getInt("ID"));
        nd.setTenNoiDung(rs.getString("TEN_NOI_DUNG"));
        nd.setTuoiDuoi(rs.getInt("TUOI_DUOI"));
        nd.setTuoiTren(rs.getInt("TUOI_TREN"));
        nd.setGioiTinh(rs.getString("GIOI_TINH"));
        nd.setTeam(rs.getBoolean("TEAM"));
        return nd;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer>[] loadCategories() {
        Map<String, Integer> singles = new LinkedHashMap<>();
        Map<String, Integer> doubles = new LinkedHashMap<>();
        if (conn == null)
            return (Map<String, Integer>[]) new Map[] { singles, doubles };

        int vernr = new Prefs().getInt("selectedGiaiDauId", -1);
        if (vernr < 0)
            return (Map<String, Integer>[]) new Map[] { singles, doubles };

        String sql1 = "SELECT nd.ID, nd.TEN_NOI_DUNG, nd.TEAM " +
                "FROM NOI_DUNG nd " +
                "JOIN CHI_TIET_GIAI_DAU ctgd ON ctgd.ID_NOI_DUNG = nd.ID " +
                "WHERE ctgd.ID_GIAI = ? " +
                "ORDER BY nd.ID ASC";
        String sql2 = "SELECT nd.ID, nd.TEN_NOI_DUNG, nd.TEAM " +
                "FROM NOI_DUNG nd " +
                "JOIN CHI_TIET_GIAI_DAU ctgd ON ctgd.ID_NOI_DUNG = nd.ID " +
                "WHERE ctgd.ID_GIAI = ? " +
                "ORDER BY nd.ID ASC";

        // Thử SQL với ID_GIAI_DAU; nếu lỗi cột không tồn tại, fallback sang ID_GIAI
        boolean loaded = false;
        for (String sql : new String[] { sql1, sql2 }) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, vernr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String tenNoiDung = rs.getString(2);
                        Object teamVal = rs.getObject(3);
                        boolean isTeam = parseTeamFlag(teamVal);
                        if (tenNoiDung != null && !tenNoiDung.isBlank()) {
                            (isTeam ? doubles : singles).put(tenNoiDung, id);
                        }
                    }
                }
                loaded = true;
                break;
            } catch (SQLException ex) {
                // thử câu tiếp theo
            }
        }
        if (!loaded) {
            JOptionPane.showMessageDialog(null,
                    "Tải \"nội dung\" từ DB lỗi: Không xác định được cấu trúc CHI_TIET_GIAI_DAU (thiếu cột ID_GIAI)",
                    "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }

        return (Map<String, Integer>[]) new Map[] { singles, doubles };
    }

    /** Chuyển giá trị TEAM về boolean */
    private boolean parseTeamFlag(Object v) {
        if (v == null)
            return false;
        if (v instanceof Boolean b)
            return b;
        if (v instanceof Number n)
            return n.intValue() != 0;
        String s = v.toString().trim();
        return s.equalsIgnoreCase("T")
                || s.equalsIgnoreCase("Y")
                || s.equalsIgnoreCase("TRUE")
                || s.equals("1");
    }
}
