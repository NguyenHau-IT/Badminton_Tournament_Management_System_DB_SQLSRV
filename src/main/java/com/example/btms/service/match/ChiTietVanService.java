package com.example.btms.service.match;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.match.ChiTietVan;
import com.example.btms.repository.match.ChiTietVanRepository;

public class ChiTietVanService {
    private final ChiTietVanRepository repo;

    public ChiTietVanService(ChiTietVanRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public void addSet(String idTranDau, int setNo, int diem1, int diem2, String dauThoiGian) {
        validateScores(diem1, diem2, dauThoiGian);
        if (exists(idTranDau, setNo))
            throw new IllegalStateException("Set đã tồn tại");
        repo.add(new ChiTietVan(idTranDau, setNo, diem1, diem2, dauThoiGian));
    }

    public ChiTietVan get(String idTranDau, int setNo) {
        ChiTietVan s = repo.findOne(idTranDau, setNo);
        if (s == null)
            throw new NoSuchElementException("Không tìm thấy set");
        return s;
    }

    public List<ChiTietVan> listByMatch(String idTranDau) {
        return repo.listByMatch(idTranDau);
    }

    public void update(String idTranDau, int setNo, int diem1, int diem2, String dauThoiGian) {
        validateScores(diem1, diem2, dauThoiGian);
        get(idTranDau, setNo);
        repo.update(new ChiTietVan(idTranDau, setNo, diem1, diem2, dauThoiGian));
    }

    public void delete(String idTranDau, int setNo) {
        get(idTranDau, setNo);
        repo.delete(idTranDau, setNo);
    }

    public boolean exists(String idTranDau, int setNo) {
        return repo.findOne(idTranDau, setNo) != null;
    }

    private void validateScores(int d1, int d2, String t) {
        if (d1 < 0 || d2 < 0)
            throw new IllegalArgumentException("Điểm không âm");
        if (t == null || t.isBlank())
            throw new IllegalArgumentException("DAU_THOI_GIAN không được rỗng");
    }
}
