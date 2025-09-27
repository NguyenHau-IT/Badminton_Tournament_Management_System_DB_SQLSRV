package com.example.btms.service.draw;

import com.example.btms.model.draw.BocThamCaNhan;
import com.example.btms.repository.draw.BocThamCaNhanRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BocThamCaNhanService {
    private final BocThamCaNhanRepository repo;

    public BocThamCaNhanService(BocThamCaNhanRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public void create(int idGiai, int idNoiDung, int idVdv, int thuTu, int soDo) {
        validate(thuTu, soDo);
        if (exists(idGiai, idNoiDung, idVdv))
            throw new IllegalStateException("VĐV đã có suất bốc thăm");
        repo.add(new BocThamCaNhan(idGiai, idNoiDung, idVdv, thuTu, soDo));
    }

    // READ
    public BocThamCaNhan getOne(int idGiai, int idNoiDung, int idVdv) {
        BocThamCaNhan r = repo.findOne(idGiai, idNoiDung, idVdv);
        if (r == null) throw new NoSuchElementException("Không tìm thấy bốc thăm cá nhân");
        return r;
    }

    public List<BocThamCaNhan> list(int idGiai, int idNoiDung) {
        return repo.list(idGiai, idNoiDung);
    }

    // UPDATE
    public void update(int idGiai, int idNoiDung, int idVdv, int thuTu, int soDo) {
        validate(thuTu, soDo);
        getOne(idGiai, idNoiDung, idVdv);
        repo.update(new BocThamCaNhan(idGiai, idNoiDung, idVdv, thuTu, soDo));
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int idVdv) {
        getOne(idGiai, idNoiDung, idVdv);
        repo.delete(idGiai, idNoiDung, idVdv);
    }

    // Helpers
    public boolean exists(int idGiai, int idNoiDung, int idVdv) {
        return repo.findOne(idGiai, idNoiDung, idVdv) != null;
    }

    private void validate(int thuTu, int soDo) {
        if (thuTu < 0) throw new IllegalArgumentException("THU_TU phải > 0");
        if (soDo <= 0) throw new IllegalArgumentException("SO_DO phải > 0");
    }
}
