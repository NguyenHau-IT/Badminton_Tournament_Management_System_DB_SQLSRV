package com.example.btms.repository.result;

import com.example.btms.model.result.KetQuaCaNhan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KetQuaCaNhanRepository {
    private final Connection conn;

    public KetQuaCaNhanRepository(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void add(KetQuaCaNhan r) {
        final String sql = "INSERT INTO KET_QUA_CA_NHAN (ID_GIAI, ID_NOI_DUNG, ID_VDV, THU_HANG) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdGiai());
            ps.setInt(2, r.getIdNoiDung());
            ps.setInt(3, r.getIdVdv());
            ps.setInt(4, r.getThuHang());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm KET_QUA_CA_NHAN", e);
        }
    }

    // READ theo khoá (thứ hạng)
    public KetQuaCaNhan findOne(int idGiai, int idNoiDung, int thuHang) {
        final String sql = "SELECT * FROM KET_QUA_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuHang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kết quả theo thứ hạng", e);
        }
    }

    // READ theo VĐV (để check VĐV đã có kết quả chưa)
    public KetQuaCaNhan findByVdv(int idGiai, int idNoiDung, int idVdv) {
        final String sql = "SELECT * FROM KET_QUA_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND ID_VDV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, idVdv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kết quả theo VĐV", e);
        }
    }

    // LIST
    public List<KetQuaCaNhan> list(int idGiai, int idNoiDung) {
        final String sql = "SELECT * FROM KET_QUA_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? ORDER BY THU_HANG";
        List<KetQuaCaNhan> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liệt kê KET_QUA_CA_NHAN", e);
        }
        return out;
    }

    // UPDATE: đổi VĐV giữ nguyên thứ hạng
    public void update(KetQuaCaNhan r) {
        final String sql = "UPDATE KET_QUA_CA_NHAN SET ID_VDV=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdVdv());
            ps.setInt(2, r.getIdGiai());
            ps.setInt(3, r.getIdNoiDung());
            ps.setInt(4, r.getThuHang());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật KET_QUA_CA_NHAN", e);
        }
    }

    // Đổi thứ hạng (đổi khoá)
    public void updateRank(int idGiai, int idNoiDung, int oldThuHang, int newThuHang) {
        final String sql = "UPDATE KET_QUA_CA_NHAN SET THU_HANG=? WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newThuHang);
            ps.setInt(2, idGiai);
            ps.setInt(3, idNoiDung);
            ps.setInt(4, oldThuHang);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đổi thứ hạng", e);
        }
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int thuHang) {
        final String sql = "DELETE FROM KET_QUA_CA_NHAN WHERE ID_GIAI=? AND ID_NOI_DUNG=? AND THU_HANG=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGiai);
            ps.setInt(2, idNoiDung);
            ps.setInt(3, thuHang);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa KET_QUA_CA_NHAN", e);
        }
    }

    private KetQuaCaNhan map(ResultSet rs) throws SQLException {
        return new KetQuaCaNhan(
                rs.getInt("ID_GIAI"),
                rs.getInt("ID_NOI_DUNG"),
                rs.getInt("ID_VDV"),
                rs.getInt("THU_HANG"));
    }
}
