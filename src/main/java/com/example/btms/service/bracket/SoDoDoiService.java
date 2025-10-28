package com.example.btms.service.bracket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.repository.bracket.SoDoDoiRepository;

public class SoDoDoiService {

    private final SoDoDoiRepository repo;

    public SoDoDoiService(SoDoDoiRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public void create(int idGiai, int idNoiDung, Integer idClb, String tenTeam,
            Integer toaDoX, Integer toaDoY, int viTri, Integer soDo,
            LocalDateTime thoiGian, Integer diem, String idTranDau) {
        validate(tenTeam, viTri);
        repo.add(new SoDoDoi(idGiai, idNoiDung, idClb, tenTeam.trim(),
                toaDoX, toaDoY, viTri, soDo, thoiGian, diem, idTranDau));
    }

    public SoDoDoi getOne(int idGiai, int idNoiDung, int viTri) {
        SoDoDoi r = repo.findOne(idGiai, idNoiDung, viTri);
        if (r == null)
            throw new NoSuchElementException("Không tìm thấy SO_DO_DOI");
        return r;
    }

    public List<SoDoDoi> list(int idGiai, int idNoiDung) {
        return repo.list(idGiai, idNoiDung);
    }

    public void update(int idGiai, int idNoiDung, int viTri,
            Integer idClb, String tenTeam, Integer toaDoX, Integer toaDoY,
            Integer soDo, LocalDateTime thoiGian, Integer diem, String idTranDau) {
        validate(tenTeam, viTri);
        getOne(idGiai, idNoiDung, viTri); // ensure exists
        repo.update(new SoDoDoi(idGiai, idNoiDung, idClb, tenTeam.trim(),
                toaDoX, toaDoY, viTri, soDo, thoiGian, diem, idTranDau));
    }

    public void delete(int idGiai, int idNoiDung, int viTri) {
        getOne(idGiai, idNoiDung, viTri);
        repo.delete(idGiai, idNoiDung, viTri);
    }

    /** Tiện ích nhanh: đổi điểm */
    public void setDiem(int idGiai, int idNoiDung, int viTri, Integer diem) {
        if (repo.updateDiem(idGiai, idNoiDung, viTri, diem) == 0)
            throw new NoSuchElementException("Không tìm thấy vị trí để cập nhật điểm");
    }

    /** Tiện ích nhanh: gán/hủy liên kết trận đấu */
    public void setTranDau(int idGiai, int idNoiDung, int viTri, String idTranDau) {
        if (repo.updateTranDau(idGiai, idNoiDung, viTri, idTranDau) == 0)
            throw new NoSuchElementException("Không tìm thấy vị trí để cập nhật ID_TRẬN");
    }

    /**
     * Liên kết ID trận cho đội trong một sơ đồ.
     * CHỈ gán vào slot trống để bảo tồn lịch sử các trận đấu trước đó.
     * Trả về số bản ghi được cập nhật (0 hoặc 1).
     */
    public int linkTranDauByTeamName(int idGiai, int idNoiDung, String tenTeam, String matchId) {
        List<SoDoDoi> rows = list(idGiai, idNoiDung);

        // Lọc ra các slot của đội này
        List<SoDoDoi> teamSlots = rows.stream()
                .filter(r -> r.getTenTeam() != null && r.getTenTeam().equalsIgnoreCase(tenTeam))
                .sorted((a, b) -> Integer.compare(b.getViTri(), a.getViTri())) // Sắp xếp theo vị trí giảm dần (vòng mới
                                                                               // nhất trước)
                .toList();

        if (teamSlots.isEmpty())
            return 0;

        // CHỈ gán vào slot trống để bảo tồn lịch sử
        for (SoDoDoi slot : teamSlots) {
            if (slot.getIdTranDau() == null) {
                return repo.updateTranDau(idGiai, idNoiDung, slot.getViTri(), matchId);
            }
        }

        // Không tìm thấy slot trống → không gán (giữ nguyên lịch sử)
        return 0;
    }

    /* helper */
    private void validate(String tenTeam, int viTri) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        if (viTri <= 0)
            throw new IllegalArgumentException("VI_TRI phải > 0");
    }
}
