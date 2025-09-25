package com.example.btms.repository.player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.btms.model.player.DangKyCaNhan;

/**
 * JDBC Repository cho bảng DANG_KI_CA_NHAN (giả định cột: ID, ID_GIAI,
 * ID_NOI_DUNG, ID_VDV).
 */
public class DangKyCaNhanRepository {
    private final Connection conn;

    public DangKyCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    public DangKyCaNhan add(DangKyCaNhan d) {
        final String sql = "INSERT INTO DANG_KI_CA_NHAN (ID_GIAI, ID_NOI_DUNG, ID_VDV, THOI_GIAN_TAO) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getIdGiai());
            ps.setInt(2, d.getIdNoiDung());
            ps.setInt(3, d.getIdVdv());
            // Lưu thời điểm tạo (ngày-tháng-năm giờ:phút:giây). LocalDateTime rõ nghĩa hơn
            // System.currentTimeMillis()
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now().withNano(0)));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    d.setId(rs.getInt(1));
            }
            return d;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm đăng ký cá nhân", e);
        }
    }

    public Optional<DangKyCaNhan> findById(int id) {
        for (String pk : PK_CANDIDATES) {
            String sql = "SELECT " + pk + " AS PK, ID_GIAI, ID_NOI_DUNG, ID_VDV FROM DANG_KI_CA_NHAN WHERE " + pk
                    + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new DangKyCaNhan(
                                rs.getInt("PK"),
                                rs.getInt("ID_GIAI"),
                                rs.getInt("ID_NOI_DUNG"),
                                rs.getInt("ID_VDV")));
                    }
                }
            } catch (SQLException ex) {
                if (!isInvalidColumnError(ex)) {
                    throw new RuntimeException("Lỗi tìm đăng ký cá nhân (pk=" + pk + ") id=" + id, ex);
                }
                // nếu invalid column thì thử pk tiếp theo
            }
        }
        return Optional.empty();
    }

    public List<DangKyCaNhan> findAllByGiai(int idGiai) {
        return loadListDynamicPk("ID_GIAI=?", ps -> ps.setInt(1, idGiai), "Lỗi tải đăng ký cá nhân theo giải");
    }

    public List<DangKyCaNhan> findAllByGiaiAndNoiDung(int idGiai, int idNoiDung) {
        return loadListDynamicPk("ID_GIAI=? AND ID_NOI_DUNG=?", ps -> {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
        }, String.format("Lỗi tải đăng ký cá nhân theo nội dung (giai=%d, noiDung=%d)", idGiai, idNoiDung));
    }

    /* ===== Dynamic PK support ===== */
    private static final String[] PK_CANDIDATES = { "ID", "ID_DANG_KI", "ID_DANG_KY", "ID_DANGKY" };

    private List<DangKyCaNhan> loadListDynamicPk(String where, SqlBinder binder, String baseError) {
        SQLException lastInvalid = null;
        for (String pk : PK_CANDIDATES) {
            String sql = "SELECT " + pk + " AS PK, ID_GIAI, ID_NOI_DUNG, ID_VDV FROM DANG_KI_CA_NHAN WHERE " + where
                    + " ORDER BY " + pk;
            List<DangKyCaNhan> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                binder.bind(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new DangKyCaNhan(
                                rs.getInt("PK"),
                                rs.getInt("ID_GIAI"),
                                rs.getInt("ID_NOI_DUNG"),
                                rs.getInt("ID_VDV")));
                    }
                }
                return list; // thành công với pk này
            } catch (SQLException ex) {
                if (isInvalidColumnError(ex)) { // thử pk khác
                    lastInvalid = ex;
                    continue;
                }
                throw new RuntimeException(baseError + " SQLState=" + ex.getSQLState() + " Code=" + ex.getErrorCode()
                        + " Msg=" + ex.getMessage(), ex);
            }
        }
        if (lastInvalid != null) {
            throw new RuntimeException(
                    baseError + ": không tìm được cột PK hợp lệ (thử: ID, ID_DANG_KI, ID_DANG_KY, ID_DANGKY)",
                    lastInvalid);
        }
        return List.of();
    }

    private boolean isInvalidColumnError(SQLException ex) {
        return ex != null && ex.getErrorCode() == 207;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
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
            throw new RuntimeException("Lỗi xóa đăng ký cá nhân id=" + id, e);
        }
    }

    private DangKyCaNhan map(ResultSet rs) throws SQLException {
        return new DangKyCaNhan(
                safeGet(rs, "ID"),
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"));
    }

    private Integer safeGet(ResultSet rs, String col) {
        try {
            return rs.getInt(col);
        } catch (SQLException ex) {
            return null;
        }
    }
}
