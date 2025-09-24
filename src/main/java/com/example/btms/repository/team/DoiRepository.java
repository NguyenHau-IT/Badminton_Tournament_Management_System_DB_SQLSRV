package com.example.btms.repository.team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.team.DangKiDoi;

public class DoiRepository {
    private final Connection conn;

    public DoiRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Lấy danh sách đội theo nội dung & giải.
     * Trả về DangKiDoi với các trường tối thiểu đã set: ID_TEAM, TEN_TEAM,
     * ID_NOI_DUNG, ID_GIAI.
     */
    public List<DangKiDoi> fetchTeamsByNoiDungVaGiai(int idNoiDung, int idGiai) {
        List<DangKiDoi> list = new ArrayList<>();

        // SQL Server: CAST về NVARCHAR để an toàn Unicode
        final String sql = "SELECT DISTINCT " +
                "  COALESCE(dkd.TEN_TEAM, CAST(dkd.ID_TEAM AS NVARCHAR(50))) AS LBL, " +
                "  dkd.ID_TEAM " +
                "FROM dbo.DANG_KI_DOI dkd " +
                "WHERE dkd.ID_NOI_DUNG = ? AND dkd.ID_GIAI = ? " +
                "ORDER BY LBL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNoiDung);
            ps.setInt(2, idGiai);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DangKiDoi d = new DangKiDoi();
                    // giả định tên setter theo quy ước Java Bean
                    d.setIdTeam(rs.getInt("ID_TEAM"));
                    d.setTenTeam(rs.getString("LBL"));
                    d.setIdNoiDung(idNoiDung);
                    d.setIdGiai(idGiai);
                    list.add(d);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Đọc danh sách đội lỗi: " + ex.getMessage(),
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /**
     * Lấy tối đa 2 VĐV của đội. Trả về mảng VanDongVien với ID_VDV & TEN_VDV đã
     * set.
     */
    public VanDongVien[] fetchTeamPlayersDetailed(int teamId) {
        List<VanDongVien> players = new ArrayList<>();

        // Lấy tối đa 2 dòng ngay trong SQL (TOP 2) để đỡ phải break trong Java
        final String sql = "SELECT TOP (2) " +
                "  ctd.ID_VDV, " +
                "  COALESCE(vdv.HO_TEN, CAST(ctd.ID_VDV AS NVARCHAR(50))) AS NM " +
                "FROM dbo.CHI_TIET_DOI ctd " +
                "LEFT JOIN dbo.VAN_DONG_VIEN vdv ON vdv.ID = ctd.ID_VDV " +
                "WHERE ctd.ID_TEAM = ? " +
                "ORDER BY ctd.ID_VDV";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VanDongVien v = new VanDongVien();
                    v.setId(rs.getInt("ID_VDV")); // NNR trước đây = ID_VDV
                    v.setHoTen(rs.getString("NM"));
                    players.add(v);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Đọc VĐV theo TEAMID lỗi: " + ex.getMessage(),
                    "Lỗi DB",
                    JOptionPane.ERROR_MESSAGE);
        }
        return players.toArray(new VanDongVien[0]);
    }
}
