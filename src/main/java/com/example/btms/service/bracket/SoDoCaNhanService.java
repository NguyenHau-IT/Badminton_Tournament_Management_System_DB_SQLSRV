package com.example.btms.service.bracket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.bracket.SoDoCaNhan;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;

public class SoDoCaNhanService {
    private final SoDoCaNhanRepository repo;

    public SoDoCaNhanService(SoDoCaNhanRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public void create(int idGiai, int idNoiDung, int idVdv,
            int toaDoX, int toaDoY, int viTri, int soDo,
            LocalDateTime thoiGian, Integer diem, String idTranDau) {
        validate(viTri, soDo);
        if (exists(idGiai, idNoiDung, viTri))
            throw new IllegalStateException("Vị trí đã có VĐV trong sơ đồ");
        repo.add(new SoDoCaNhan(idGiai, idNoiDung, idVdv, toaDoX, toaDoY, viTri, soDo, thoiGian, diem, idTranDau));
    }

    // READ
    public SoDoCaNhan getOne(int idGiai, int idNoiDung, int viTri) {
        SoDoCaNhan r = repo.findOne(idGiai, idNoiDung, viTri);
        if (r == null)
            throw new NoSuchElementException("Không tìm thấy vị trí trong sơ đồ cá nhân");
        return r;
    }

    public List<SoDoCaNhan> list(int idGiai, int idNoiDung) {
        return repo.list(idGiai, idNoiDung);
    }

    // UPDATE
    public void update(int idGiai, int idNoiDung, int viTri,
            int idVdv, int toaDoX, int toaDoY, int soDo,
            LocalDateTime thoiGian, Integer diem, String idTranDau) {
        validate(viTri, soDo);
        getOne(idGiai, idNoiDung, viTri); // ensure exists
        repo.update(
                new SoDoCaNhan(idGiai, idNoiDung, idVdv, toaDoX, toaDoY, viTri, soDo, thoiGian, diem, idTranDau));
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int viTri) {
        getOne(idGiai, idNoiDung, viTri);
        repo.delete(idGiai, idNoiDung, viTri);
    }

    // PATCH tiện ích
    public void setDiem(int idGiai, int idNoiDung, int viTri, Integer diem) {
        if (repo.updateDiem(idGiai, idNoiDung, viTri, diem) == 0)
            throw new NoSuchElementException("Không tìm thấy vị trí để cập nhật điểm");
    }

    public void setTranDau(int idGiai, int idNoiDung, int viTri, String idTranDau) {
        if (repo.updateTranDau(idGiai, idNoiDung, viTri, idTranDau) == 0)
            throw new NoSuchElementException("Không tìm thấy vị trí để cập nhật ID_TRẬN");
    }

    /**
     * Liên kết ID trận cho VĐV trong một sơ đồ.
     * CHỈ gán vào slot trống để bảo tồn lịch sử các trận đấu trước đó.
     * Trả về số bản ghi được cập nhật (0 hoặc 1).
     */
    public int linkTranDauByVdv(int idGiai, int idNoiDung, int idVdv, String matchId) {
        List<SoDoCaNhan> rows = list(idGiai, idNoiDung);

        // Lọc ra các slot của VĐV này
        List<SoDoCaNhan> vdvSlots = rows.stream()
                .filter(r -> r.getIdVdv() != null && r.getIdVdv() == idVdv)
                .sorted((a, b) -> Integer.compare(b.getViTri(), a.getViTri())) // Sắp xếp theo vị trí giảm dần (vòng mới
                                                                               // nhất trước)
                .toList();

        if (vdvSlots.isEmpty())
            return 0;

        // CHỈ gán vào slot trống để bảo tồn lịch sử
        for (SoDoCaNhan slot : vdvSlots) {
            if (slot.getIdTranDau() == null) {
                return repo.updateTranDau(idGiai, idNoiDung, slot.getViTri(), matchId);
            }
        }

        // Không tìm thấy slot trống → không gán (giữ nguyên lịch sử)
        return 0;
    }

    // Helpers
    public boolean exists(int idGiai, int idNoiDung, int viTri) {
        return repo.findOne(idGiai, idNoiDung, viTri) != null;
    }

    private void validate(int viTri, int soDo) {
        if (viTri < 0)
            throw new IllegalArgumentException("VI_TRI phải >= 0");
        if (soDo <= 0)
            throw new IllegalArgumentException("SO_DO phải > 0");
    }
}
