package com.example.btms.repository.draw;

import com.example.btms.model.draw.BocThamCaNhan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BocThamCaNhanRepository {
    private final Connection conn;

    public BocThamCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void add(BocThamCaNhan r) {
        String sql = "INSERT INTO BOC_THAM_CA_NHAN (ID_GIAI, ID_NOI_DUNG, ID_VDV, THU_TU, SO_DO) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdVdv());
            ps.setInt(4, r.getThuTu());
            ps.setInt(5, r.getSoDo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm BOC_THAM_CA_NHAN", e);
        }
    }

    // READ
    public BocThamCaNhan findOne(int idGiai, int idNoiDung, int idVdv) {
        String sql = "SELECT * FROM BOC_THAM_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm BOC_THAM_CA_NHAN", e);
        }
    }

    // LIST
    public List<BocThamCaNhan> list(int idGiai, int idNoiDung) {
        String sql = "SELECT * FROM BOC_THAM_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY THU_TU";
        List<BocThamCaNhan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi list BOC_THAM_CA_NHAN", e);
        }
        return out;
    }

    // UPDATE
    public void update(BocThamCaNhan r) {
        String sql = "UPDATE BOC_THAM_CA_NHAN SET THU_TU=?, SO_DO=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getThuTu());
            ps.setInt(2, r.getSoDo());
            ps.setInt(3, r.getIdGiai());
            ps.setInt(4, r.getIdNoiDung());
            ps.setInt(5, r.getIdVdv());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update BOC_THAM_CA_NHAN", e);
        }
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int idVdv) {
        String sql = "DELETE FROM BOC_THAM_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi delete BOC_THAM_CA_NHAN", e);
        }
    }

    private BocThamCaNhan map(ResultSet rs) throws SQLException {
        return new BocThamCaNhan(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"),
                rs.getInt("THU_TU"),
                rs.getInt("SO_DO")
        );
    }
}