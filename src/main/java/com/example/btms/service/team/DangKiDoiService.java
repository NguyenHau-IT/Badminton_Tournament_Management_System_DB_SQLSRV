package com.example.btms.service.team;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.team.DangKiDoiRepository;

public class DangKiDoiService {

    private final DangKiDoiRepository teamRepo;

    public DangKiDoiService(DangKiDoiRepository teamRepo) {
        this.teamRepo = Objects.requireNonNull(teamRepo);
    }

    /** Tạo đội (chưa đụng tới thành viên) */
    public int createTeam(int idGiai, int idNoiDung, Integer idClb, String tenTeam) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        DangKiDoi t = teamRepo.add(new DangKiDoi(idGiai, idNoiDung, idClb, tenTeam.trim()));
        return t.getIdTeam();
    }

    /** Cập nhật tên đội/CLB */
    public void updateTeamInfo(int idTeam, Integer idClb, String tenTeam) {
        DangKiDoi current = requireTeam(idTeam);
        if (tenTeam != null) {
            if (tenTeam.trim().isEmpty())
                throw new IllegalArgumentException("TEN_TEAM không được rỗng");
            current.setTenTeam(tenTeam.trim());
        }
        current.setIdCauLacBo(idClb);
        teamRepo.update(current);
    }

    /** Xoá đội (chỉ bảng đội). Nên bật FK ON DELETE CASCADE cho bảng chi tiết */
    public void deleteTeam(int idTeam) {
        requireTeam(idTeam);
        teamRepo.delete(idTeam);
    }

    /** Xóa tất cả đội theo giải */
    public int deleteAllByGiai(int idGiai) {
        return teamRepo.deleteAllByGiai(idGiai);
    }

    /** Xóa tất cả đội theo giải và nội dung */
    public int deleteAllByGiaiAndNoiDung(int idGiai, int idNoiDung) {
        return teamRepo.deleteAllByGiaiAndNoiDung(idGiai, idNoiDung);
    }

    /** Lấy 1 đội */
    public DangKiDoi getTeam(int idTeam) {
        return requireTeam(idTeam);
    }

    /** Liệt kê các đội trong (giải, nội dung) */
    public List<DangKiDoi> listTeams(int idGiai, int idNoiDung) {
        return teamRepo.findAllBy(idGiai, idNoiDung);
    }

    /** Đổi nội dung (category) cho một đội đã đăng ký. */
    public void updateTeamCategory(int idTeam, int newIdNoiDung) {
        DangKiDoi current = requireTeam(idTeam);
        current.setIdNoiDung(newIdNoiDung);
        teamRepo.update(current);
    }

    /* ===== helpers ===== */
    private DangKiDoi requireTeam(int idTeam) {
        DangKiDoi t = teamRepo.findById(idTeam);
        if (t == null)
            throw new NoSuchElementException("Không tìm thấy đội ID_TEAM=" + idTeam);
        return t;
    }
}
