package com.example.badmintoneventtechnology.service.team;

import com.example.badmintoneventtechnology.model.team.DangKiDoi;
import com.example.badmintoneventtechnology.model.team.ChiTietDoi;
import com.example.badmintoneventtechnology.repository.team.DangKiDoiRepository;
import com.example.badmintoneventtechnology.repository.team.ChiTietDoiRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DangKiDoiService {
    private final Connection conn; // dùng cho transaction
    private final DangKiDoiRepository teamRepo;
    private final ChiTietDoiRepository detailRepo;

    public DangKiDoiService(Connection conn,
            DangKiDoiRepository teamRepo,
            ChiTietDoiRepository detailRepo) {
        this.conn = Objects.requireNonNull(conn);
        this.teamRepo = Objects.requireNonNull(teamRepo);
        this.detailRepo = Objects.requireNonNull(detailRepo);
    }

    /** Tạo đội mới + gắn 2 VĐV (transaction). Trả về ID_TEAM. */
    public int createTeam(int idGiai, int idNoiDung, Integer idClb, String tenTeam, List<Integer> vdvIds) {
        validateTeamInput(tenTeam, vdvIds);

        boolean oldAuto = true;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            DangKiDoi t = teamRepo.add(new DangKiDoi(idGiai, idNoiDung, idClb, tenTeam.trim()));
            detailRepo.addMembers(t.getIdTeam(), vdvIdsDistinct(vdvIds));

            conn.commit();
            return t.getIdTeam();
        } catch (RuntimeException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Tạo đội thất bại: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    /** Đổi tên hoặc CLB của đội. */
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

    /** Thay toàn bộ thành viên đội (transaction). Mặc định 2 người. */
    public void replaceMembers(int idTeam, List<Integer> newVdvIds) {
        validateMembers(newVdvIds);

        boolean oldAuto = true;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            requireTeam(idTeam);
            detailRepo.removeAll(idTeam);
            detailRepo.addMembers(idTeam, vdvIdsDistinct(newVdvIds));

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

    /** Xóa đội (xóa cả chi tiết trước, nếu DB không bật ON DELETE CASCADE). */
    public void deleteTeam(int idTeam) {
        boolean oldAuto = true;
        try {
            oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            requireTeam(idTeam);
            detailRepo.removeAll(idTeam);
            teamRepo.delete(idTeam);

            conn.commit();
        } catch (RuntimeException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Xóa đội thất bại: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    /** Lấy đội + danh sách thành viên. */
    public Map<String, Object> getTeamWithMembers(int idTeam) {
        DangKiDoi t = requireTeam(idTeam);
        List<ChiTietDoi> members = detailRepo.findByTeam(idTeam);
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("team", t);
        dto.put("members", members);
        return dto;
    }

    /** Liệt kê các đội của 1 (giải, nội dung). */
    public List<DangKiDoi> listTeams(int idGiai, int idNoiDung) {
        return teamRepo.findAllBy(idGiai, idNoiDung);
    }

    /* ===================== helpers ===================== */

    private DangKiDoi requireTeam(int idTeam) {
        DangKiDoi t = teamRepo.findById(idTeam);
        if (t == null)
            throw new NoSuchElementException("Không tìm thấy đội ID_TEAM=" + idTeam);
        return t;
    }

    private void validateTeamInput(String tenTeam, List<Integer> vdvIds) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        validateMembers(vdvIds);
    }

    private void validateMembers(List<Integer> vdvIds) {
        if (vdvIds == null || vdvIds.size() != 2)
            throw new IllegalArgumentException("Đội phải có đúng 2 VĐV");
        if (vdvIds.get(0) == null || vdvIds.get(1) == null)
            throw new IllegalArgumentException("ID_VDV không được null");
        if (Objects.equals(vdvIds.get(0), vdvIds.get(1)))
            throw new IllegalArgumentException("Hai VĐV trong đội phải khác nhau");
    }

    private List<Integer> vdvIdsDistinct(List<Integer> ids) {
        return ids.stream().distinct().collect(Collectors.toList());
    }
}
