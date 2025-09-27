package com.example.btms.service.team;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.team.DoiRepository;

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

    /** Lấy tên đội theo ID. */
    public String getClubNameByTeamId(int teamId) {
        return teamRepo.fetchClubNameById(teamId);
    }

    /** Lấy Id_CLB theo tên team **/
    public int getIdClbByTeamName(String teamName, int idNoiDung, int idGiai) {
        return teamRepo.fetchIdClbByTeamName(teamName, idNoiDung, idGiai);
    }
}
