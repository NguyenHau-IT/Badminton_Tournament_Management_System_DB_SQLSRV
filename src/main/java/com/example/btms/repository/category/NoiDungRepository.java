package com.example.btms.repository.category;

import com.example.btms.model.category.NoiDung;

import java.sql.*;
import java.util.*;

public class NoiDungRepository {
    private final Connection connection;

    public NoiDungRepository(Connection connection) {
        this.connection = connection;
    }

    public NoiDung create(NoiDung noiDung) throws SQLException {
        String sql = "INSERT INTO NOI_DUNG (TEN_NOI_DUNG, TUOI_DUOI, TUOI_TREN, GIOI_TINH, TEAM) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNoiDung(rs));
            }
        }
        return list;
    }

    public Optional<NoiDung> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM NOI_DUNG WHERE ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
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
}
