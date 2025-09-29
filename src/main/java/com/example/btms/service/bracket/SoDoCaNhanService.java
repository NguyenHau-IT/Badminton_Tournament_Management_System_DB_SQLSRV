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
            int toaDoX, int toaDoY, int viTri, int soDo, LocalDateTime thoiGian) {
        validate(viTri, soDo, thoiGian);
        if (exists(idGiai, idNoiDung, viTri))
            throw new IllegalStateException("Vị trí đã có VĐV trong sơ đồ");
        repo.add(new SoDoCaNhan(idGiai, idNoiDung, idVdv, toaDoX, toaDoY, viTri, soDo, thoiGian));
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
            int idVdv, int toaDoX, int toaDoY, int soDo, LocalDateTime thoiGian) {
        validate(viTri, soDo, thoiGian);
        getOne(idGiai, idNoiDung, viTri);
        repo.update(new SoDoCaNhan(idGiai, idNoiDung, idVdv, toaDoX, toaDoY, viTri, soDo, thoiGian));
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int viTri) {
        getOne(idGiai, idNoiDung, viTri);
        repo.delete(idGiai, idNoiDung, viTri);
    }

    // Helpers
    public boolean exists(int idGiai, int idNoiDung, int viTri) {
        return repo.findOne(idGiai, idNoiDung, viTri) != null;
    }

    private void validate(int viTri, int soDo, LocalDateTime thoiGian) {
        if (viTri < 0)
            throw new IllegalArgumentException("VI_TRI phải >= 0");
        if (soDo <= 0)
            throw new IllegalArgumentException("SO_DO phải > 0");
        if (thoiGian == null)
            throw new IllegalArgumentException("THOI_GIAN không được null");
    }
}
