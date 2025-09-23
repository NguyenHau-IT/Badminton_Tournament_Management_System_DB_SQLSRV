package com.example.badmintoneventtechnology.repository.tuornament;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.badmintoneventtechnology.model.tournament.GiaiDau;

/**
 * Repository class cho CRUD operations với bảng GIAI_DAU
 */
public class GiaiDauRepository {
    private final Connection connection;

    public GiaiDauRepository(Connection connection) {
        this.connection = connection;
    }

    /** Tạo giải đấu mới (CREATE) */
    public GiaiDau create(GiaiDau giaiDau) throws SQLException {
        String sql = """
                INSERT INTO GIAI_DAU (TEN_GIAI, NGAY_BD, NGAY_KT, NGAY_TAO, NGAY_CAP_NHAT, ID_USER)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, giaiDau.getTenGiai());

            // NGAY_BD, NGAY_KT: model là LocalDate -> lưu TIMESTAMP ở 00:00
            LocalDate ngayBd = giaiDau.getNgayBd(); // LocalDate (có thể null)
            LocalDate ngayKt = giaiDau.getNgayKt(); // LocalDate (có thể null)
            pstmt.setTimestamp(2, ngayBd != null ? Timestamp.valueOf(ngayBd.atStartOfDay()) : null);
            pstmt.setTimestamp(3, ngayKt != null ? Timestamp.valueOf(ngayKt.atStartOfDay()) : null);

            // NGAY_TAO, NGAY_CAP_NHAT: giữ LocalDateTime
            pstmt.setTimestamp(4, Timestamp.valueOf(giaiDau.getNgayTao()));
            pstmt.setTimestamp(5, Timestamp.valueOf(giaiDau.getNgayCapNhat()));
            pstmt.setLong(6, giaiDau.getIdUser());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating giai dau failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    giaiDau.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating giai dau failed, no ID obtained.");
                }
            }
        }

        return giaiDau;
    }

    /** Lấy tất cả giải đấu (READ - All) */
    public List<GiaiDau> findAll() throws SQLException {
        List<GiaiDau> giaiDaus = new ArrayList<>();
        String sql = "SELECT * FROM GIAI_DAU ORDER BY NGAY_TAO DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                giaiDaus.add(mapResultSetToGiaiDau(rs));
            }
        }

        return giaiDaus;
    }

    /** Lấy giải đấu theo ID (READ - By ID) */
    public Optional<GiaiDau> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM GIAI_DAU WHERE ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGiaiDau(rs));
                }
            }
        }

        return Optional.empty();
    }

    /** Lấy giải đấu theo tên (READ - By Name) */
    public List<GiaiDau> findByName(String tenGiai) throws SQLException {
        List<GiaiDau> giaiDaus = new ArrayList<>();
        String sql = "SELECT * FROM GIAI_DAU WHERE TEN_GIAI LIKE ? ORDER BY NGAY_TAO DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + tenGiai + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    giaiDaus.add(mapResultSetToGiaiDau(rs));
                }
            }
        }

        return giaiDaus;
    }

    /** Lấy giải đấu theo User ID */
    public List<GiaiDau> findByUserId(Long userId) throws SQLException {
        List<GiaiDau> giaiDaus = new ArrayList<>();
        String sql = "SELECT * FROM GIAI_DAU WHERE ID_USER = ? ORDER BY NGAY_TAO DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    giaiDaus.add(mapResultSetToGiaiDau(rs));
                }
            }
        }

        return giaiDaus;
    }

    /** Cập nhật giải đấu (UPDATE) */
    public boolean update(GiaiDau giaiDau) throws SQLException {
        String sql = """
                UPDATE GIAI_DAU
                SET TEN_GIAI = ?, NGAY_BD = ?, NGAY_KT = ?, NGAY_CAP_NHAT = ?, ID_USER = ?
                WHERE ID = ?
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, giaiDau.getTenGiai());

            LocalDate ngayBd = giaiDau.getNgayBd();
            LocalDate ngayKt = giaiDau.getNgayKt();
            pstmt.setTimestamp(2, ngayBd != null ? Timestamp.valueOf(ngayBd.atStartOfDay()) : null);
            pstmt.setTimestamp(3, ngayKt != null ? Timestamp.valueOf(ngayKt.atStartOfDay()) : null);

            pstmt.setTimestamp(4, Timestamp.valueOf(giaiDau.getNgayCapNhat()));
            pstmt.setLong(5, giaiDau.getIdUser());
            pstmt.setLong(6, giaiDau.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /** Xóa giải đấu (DELETE) */
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM GIAI_DAU WHERE ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /** Đếm số lượng giải đấu */
    public Integer count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM GIAI_DAU";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    /** Kiểm tra giải đấu có tồn tại không */
    public boolean exists(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM GIAI_DAU WHERE ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        }
    }

    /** Map ResultSet thành GiaiDau object */
    private GiaiDau mapResultSetToGiaiDau(ResultSet rs) throws SQLException {
        GiaiDau giaiDau = new GiaiDau();
        giaiDau.setId(rs.getInt("ID"));
        giaiDau.setTenGiai(rs.getString("TEN_GIAI"));

        // Đọc TIMESTAMP -> LocalDate
        Timestamp ngayBdTs = rs.getTimestamp("NGAY_BD");
        if (ngayBdTs != null) {
            LocalDate d = ngayBdTs.toLocalDateTime().toLocalDate();
            giaiDau.setNgayBd(d);
        }

        Timestamp ngayKtTs = rs.getTimestamp("NGAY_KT");
        if (ngayKtTs != null) {
            LocalDate d = ngayKtTs.toLocalDateTime().toLocalDate();
            giaiDau.setNgayKt(d);
        }

        // Audit fields giữ LocalDateTime
        giaiDau.setNgayTao(rs.getTimestamp("NGAY_TAO").toLocalDateTime());
        giaiDau.setNgayCapNhat(rs.getTimestamp("NGAY_CAP_NHAT").toLocalDateTime());

        int idUser = rs.getInt("ID_USER");
        if (!rs.wasNull()) {
            giaiDau.setIdUser(idUser);
        }

        return giaiDau;
    }
}
