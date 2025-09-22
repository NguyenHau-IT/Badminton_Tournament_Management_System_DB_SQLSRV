package com.example.badmintoneventtechnology.service.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.example.badmintoneventtechnology.model.tournament.Tournament;

public class TournamentRepository {
    private final Connection conn;

    public TournamentRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Lấy danh sách tất cả giải đấu từ bảng VERANSTALTUNG
     * 
     * @return List các giải đấu, sắp xếp theo tên
     */
    public List<Tournament> loadAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        final String sql = "SELECT VERNR, BEZEICHNUNG FROM PUBLIC.VERANSTALTUNG ORDER BY VERNR";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int vernr = rs.getInt("VERNR");
                String bezeichnung = rs.getString("BEZEICHNUNG");
                if (bezeichnung != null && !bezeichnung.trim().isEmpty()) {
                    tournaments.add(new Tournament(vernr, bezeichnung));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Lỗi khi tải danh sách giải đấu: " + ex.getMessage(),
                    "Lỗi Database",
                    JOptionPane.ERROR_MESSAGE);
        }

        return tournaments;
    }

    /**
     * Lấy thông tin giải đấu theo ID
     * 
     * @param vernr ID giải đấu
     * @return Tournament object hoặc null nếu không tìm thấy
     */
    public Tournament getTournamentById(int vernr) {
        final String sql = "SELECT VERNR, BEZEICHNUNG FROM PUBLIC.VERANSTALTUNG WHERE VERNR = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vernr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String bezeichnung = rs.getString("BEZEICHNUNG");
                    return new Tournament(vernr, bezeichnung);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Lỗi khi tải thông tin giải đấu: " + ex.getMessage(),
                    "Lỗi Database",
                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }
}
