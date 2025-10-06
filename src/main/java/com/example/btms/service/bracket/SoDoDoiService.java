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
            LocalDateTime thoiGian, Integer diem, Integer idTranDau) {
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
            Integer soDo, LocalDateTime thoiGian, Integer diem, Integer idTranDau) {
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
    public void setTranDau(int idGiai, int idNoiDung, int viTri, Integer idTranDau) {
        if (repo.updateTranDau(idGiai, idNoiDung, viTri, idTranDau) == 0)
            throw new NoSuchElementException("Không tìm thấy vị trí để cập nhật ID_TRẬN");
    }

    /* helper */
    private void validate(String tenTeam, int viTri) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        if (viTri <= 0)
            throw new IllegalArgumentException("VI_TRI phải > 0");
    }
}
