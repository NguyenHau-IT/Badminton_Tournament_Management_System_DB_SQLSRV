package com.example.badmintoneventtechnology.service.team;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import com.example.badmintoneventtechnology.model.player.VanDongVien;
import com.example.badmintoneventtechnology.model.team.DangKiDoi;
import com.example.badmintoneventtechnology.repository.team.DoiRepository;

public class DoiService {
    private final Connection conn; // có thể dùng cho nghiệp vụ cần transaction
    private final DoiRepository teamRepo;

    public DoiService(Connection conn) {
        this.conn = Objects.requireNonNull(conn, "conn must not be null");
        this.teamRepo = new DoiRepository(conn);
    }

    /** Lấy danh sách đội theo nội dung & giải. */
    public List<DangKiDoi> getTeamsByNoiDungVaGiai(int idNoiDung, int idGiai) {
        return teamRepo.fetchTeamsByNoiDungVaGiai(idNoiDung, idGiai);
    }

    /** Lấy tối đa 2 VĐV của 1 đội. */
    public VanDongVien[] getTeamPlayers(int teamId) {
        return teamRepo.fetchTeamPlayersDetailed(teamId);
    }
}
