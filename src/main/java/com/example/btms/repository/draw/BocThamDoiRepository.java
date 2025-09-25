package com.example.btms.repository.draw;

import com.example.btms.model.draw.BocThamDoi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BocThamDoiRepository {
    private final Connection conn;

    public BocThamDoiRepository(Connection conn) {
        this.conn = conn;
    }

    /** CREATE: thêm 1 dòng bốc thăm */
    public void add(BocThamDoi row) {
        final String sql = "INSERT INTO BOC_THAM_DOI " +
                "(ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, THU_TU, SO_DO) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, row.getIdGiai());
            ps.setInt(2, row.getIdNoiDung());
            // Không cho phép null -> mặc định 0 nếu thiếu
            ps.setInt(3, row.getIdClb() != null ? row.getIdClb() : 0);
            ps.setString(4, row.getTenTeam());
            ps.setInt(5, row.getThuTu());
            // SO_DO mặc định = 1 nếu null
            ps.setInt(6, row.getSoDo() != null ? row.getSoDo() : 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm BOC_THAM_DOI", e);
        }
    }

    /** READ: lấy theo khóa (idGiai, idNoiDung, thuTu) */
    public BocThamDoi findOne(int idGiai, int idNoiDung, int thuTu) {
        final String sql = "SELECT ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, THU_TU, SO_DO " +
                "FROM BOC_THAM_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_TU=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuTu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm dòng bốc thăm", e);
        }
    }

    /** READ: danh sách theo (giải, nội dung) sắp theo THU_TU */
    public List<BocThamDoi> listBy(int idGiai, int idNoiDung) {
        final String sql = "SELECT ID_GIAI, ID_NOI_DUNG, ID_CLB, TEN_TEAM, THU_TU, SO_DO " +
                "FROM BOC_THAM_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY THU_TU";
        List<BocThamDoi> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê BOC_THAM_DOI", e);
        }
        return out;
    }

    /** UPDATE: cập nhật thông tin (không đổi khóa THU_TU) */
    public void update(BocThamDoi row) {
        final String sql = "UPDATE BOC_THAM_DOI SET ID_CLB=?, TEN_TEAM=?, SO_DO=? " +
                "WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_TU=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, row.getIdClb() != null ? row.getIdClb() : 0);
            ps.setString(2, row.getTenTeam());
            ps.setInt(3, row.getSoDo() != null ? row.getSoDo() : 1);
            ps.setInt(4, row.getIdGiai());
            ps.setInt(5, row.getIdNoiDung());
            ps.setInt(6, row.getThuTu());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật BOC_THAM_DOI", e);
        }
    }

    /** Đổi THU_TU (đổi khóa). Tùy RDBMS có thể cần xử lý xung đột trước. */
    public void updateOrder(int idGiai, int idNoiDung, int oldThuTu, int newThuTu) {
        final String sql = "UPDATE BOC_THAM_DOI SET THU_TU=? " +
                "WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_TU=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newThuTu);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, oldThuTu);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đổi thứ tự bốc thăm", e);
        }
    }

    /** DELETE: xóa 1 dòng theo khóa */
    public void delete(int idGiai, int idNoiDung, int thuTu) {
        final String sql = "DELETE FROM BOC_THAM_DOI WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_TU=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuTu);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa BOC_THAM_DOI", e);
        }
    }

    /* mapper */
    private BocThamDoi map(ResultSet rs) throws SQLException {
        Integer idClb = rs.getObject("ID_CLB") != null ? rs.getInt("ID_CLB") : 0; // default 0
        Integer soDo = rs.getObject("SO_DO") != null ? rs.getInt("SO_DO") : 1; // default 1
        return new BocThamDoi(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                idClb,
                rs.getString("TEN_TEAM"),
                rs.getInt("THU_TU"),
                soDo);
    }
}
