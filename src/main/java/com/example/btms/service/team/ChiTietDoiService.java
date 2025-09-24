package com.example.btms.service.team;

import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ChiTietDoiService {

    private final Connection conn; // dùng cho các thao tác cần transaction
    private final DangKiDoiRepository teamRepo; // để kiểm tra đội tồn tại
    private final ChiTietDoiRepository detailRepo;

    public ChiTietDoiService(Connection conn,
            DangKiDoiRepository teamRepo,
            ChiTietDoiRepository detailRepo) {
        this.conn = Objects.requireNonNull(conn);
        this.teamRepo = Objects.requireNonNull(teamRepo);
        this.detailRepo = Objects.requireNonNull(detailRepo);
    }

    /** Thêm 1 thành viên vào đội */
    public void addMember(int idTeam, int idVdv) {
        ensureTeamExists(idTeam);
        detailRepo.addMember(idTeam, idVdv);
    }

    /** Thêm nhiều thành viên 1 lần */
    public void addMembers(int idTeam, List<Integer> vdvIds) {
        ensureTeamExists(idTeam);
        if (vdvIds == null || vdvIds.isEmpty())
            throw new IllegalArgumentException("Danh sách VĐV trống");
        detailRepo.addMembers(idTeam, vdvIds);
    }

    /** Thay toàn bộ thành viên đội (mặc định yêu cầu đúng 2 VĐV) */
    public void replaceMembers(int idTeam, List<Integer> newVdvIds) {
        ensureTeamExists(idTeam);
        validateTwoMembers(newVdvIds);

        boolean oldAuto = true;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            detailRepo.removeAll(idTeam);
            detailRepo.addMembers(idTeam, newVdvIds);

            conn.commit();
        } catch (RuntimeException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Cập nhật thành viên đội thất bại: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    /** Xoá 1 thành viên */
    public void removeMember(int idTeam, int idVdv) {
        ensureTeamExists(idTeam);
        detailRepo.removeMember(idTeam, idVdv);
    }

    /** Xoá toàn bộ thành viên của đội */
    public void removeAll(int idTeam) {
        ensureTeamExists(idTeam);
        detailRepo.removeAll(idTeam);
    }

    /** Lấy danh sách thành viên của đội */
    public List<ChiTietDoi> listMembers(int idTeam) {
        ensureTeamExists(idTeam);
        return detailRepo.findByTeam(idTeam);
    }

    /* ===== helpers ===== */
    private void ensureTeamExists(int idTeam) {
        if (teamRepo.findById(idTeam) == null)
            throw new IllegalArgumentException("Đội không tồn tại: ID_TEAM=" + idTeam);
    }

    /** Nếu bạn muốn đội bắt buộc 2 VĐV thì bật validate này. Nếu không thì bỏ. */
    private void validateTwoMembers(List<Integer> ids) {
        if (ids == null || ids.size() != 2)
            throw new IllegalArgumentException("Đội phải có đúng 2 VĐV");
        if (ids.get(0) == null || ids.get(1) == null)
            throw new IllegalArgumentException("ID_VDV không được null");
        if (ids.get(0).equals(ids.get(1)))
            throw new IllegalArgumentException("Hai VĐV trong đội phải khác nhau");
    }
}
