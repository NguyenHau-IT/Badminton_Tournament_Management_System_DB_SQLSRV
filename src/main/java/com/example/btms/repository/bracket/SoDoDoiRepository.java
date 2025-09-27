package com.example.btms.repository.bracket;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.bracket.SoDoDoi;

public class SoDoDoiRepository {
    private final Connection conn;

    public SoDoDoiRepository(Connection conn) {
        this.conn = conn;
    }

    public int add(SoDoDoi row) {
        final String sql = "INSERT INTO SO_DO_DOI " +
                "(ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, TOA_DO_X, TOA_DO_Y, VI_TRI, SO_DO, THOI_GIAN) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // TODO: validate nếu cần
        // Objects.requireNonNull(row.getTenTeam(), "TEN_TEAM không được null");
        // Objects.requireNonNull(row.getViTri(), "VI_TRI không được null");

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, row.getIdGiai());
            ps.setInt(2, row.getIdNoiDung());

            // Integer -> nullable int
            if (row.getIdClb() == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, row.getIdClb());

            ps.setString(4, row.getTenTeam());

            if (row.getToaDoX() == null)
                ps.setNull(5, Types.INTEGER);
            else
                ps.setInt(5, row.getToaDoX());

            if (row.getToaDoY() == null)
                ps.setNull(6, Types.INTEGER);
            else
                ps.setInt(6, row.getToaDoY());

            ps.setInt(7, row.getViTri()); // giả định không null

            if (row.getSoDo() == null)
                ps.setNull(8, Types.INTEGER);
            else
                ps.setInt(8, row.getSoDo());

            // THOI_GIAN: ưu tiên setObject để tương thích DATETIME2/OffsetDateTime
            if (row.getThoiGian() == null) {
                ps.setNull(9, Types.TIMESTAMP);
            } else {
                // Nếu là LocalDateTime:
                // ps.setTimestamp(9, Timestamp.valueOf(row.getThoiGian()));
                // Nếu là OffsetDateTime/Instant/LocalDateTime: dùng setObject cho gọn
                ps.setObject(9, row.getThoiGian());
            }

            int affected = ps.executeUpdate();

            // Nếu cần lấy khóa tự tăng:
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return affected; // hoặc trả về 1/0 nếu bạn không cần id
        } catch (SQLException e) {
            // Nếu dùng SQL Server: 2601 (duplicate key), 2627 (unique constraint)
            // Có thể phân nhánh để báo lỗi rõ hơn
            // if (e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode()
            // == 2601 || e.getErrorCode() == 2627) { ... }
            throw new RuntimeException(
                    "Lỗi thêm SO_DO_DOI (SQLState=" + e.getSQLState() + ", Code=" + e.getErrorCode() + "): "
                            + e.getMessage(),
                    e);
        }
    }

    public SoDoDoi findOne(int idGiai, int idNoiDung, int viTri) {
        String sql = "SELECT * FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm SO_DO_DOI", e);
        }
    }

    public List<SoDoDoi> list(int idGiai, int idNoiDung) {
        String sql = "SELECT * FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY VI_TRI";
        List<SoDoDoi> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê SO_DO_DOI", e);
        }
        return list;
    }

    public void update(SoDoDoi row) {
        String sql = "UPDATE SO_DO_DOI SET ID_CLB=?, TEN_TEAM=?, TOA_DO_X=?, TOA_DO_Y=?, SO_DO=?, THOI_GIAN=? " +
                "WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (row.getIdClb() != null)
                ps.setInt(1, row.getIdClb());
            else
                ps.setNull(1, Types.INTEGER);
            ps.setString(2, row.getTenTeam());
            if (row.getToaDoX() != null)
                ps.setInt(3, row.getToaDoX());
            else
                ps.setNull(3, Types.INTEGER);
            if (row.getToaDoY() != null)
                ps.setInt(4, row.getToaDoY());
            else
                ps.setNull(4, Types.INTEGER);
            if (row.getSoDo() != null)
                ps.setInt(5, row.getSoDo());
            else
                ps.setNull(5, Types.INTEGER);
            if (row.getThoiGian() != null)
                ps.setTimestamp(6, Timestamp.valueOf(row.getThoiGian()));
            else
                ps.setNull(6, Types.TIMESTAMP);

            ps.setInt(7, row.getIdGiai());
            ps.setInt(8, row.getIdNoiDung());
            ps.setInt(9, row.getViTri());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật SO_DO_DOI", e);
        }
    }

    public void delete(int idGiai, int idNoiDung, int viTri) {
        String sql = "DELETE FROM SO_DO_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND VI_TRI=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, viTri);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa SO_DO_DOI", e);
        }
    }

    private SoDoDoi map(ResultSet rs) throws SQLException {
        return new SoDoDoi(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getObject("ID_CLB") != null ? rs.getInt("ID_CLB") : null,
                rs.getString("TEN_TEAM"),
                rs.getObject("TOA_DO_X") != null ? rs.getInt("TOA_DO_X") : null,
                rs.getObject("TOA_DO_Y") != null ? rs.getInt("TOA_DO_Y") : null,
                rs.getInt("VI_TRI"),
                rs.getObject("SO_DO") != null ? rs.getInt("SO_DO") : null,
                rs.getTimestamp("THOI_GIAN") != null ? rs.getTimestamp("THOI_GIAN").toLocalDateTime() : null);
    }
}
