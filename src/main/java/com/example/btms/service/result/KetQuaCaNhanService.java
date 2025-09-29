package com.example.btms.service.result;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.result.KetQuaCaNhan;
import com.example.btms.repository.result.KetQuaCaNhanRepository;

public class KetQuaCaNhanService {
    private final KetQuaCaNhanRepository repo;

    public KetQuaCaNhanService(KetQuaCaNhanRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public void create(int idGiai, int idNoiDung, int idVdv, int thuHang) {
        validate(idVdv, thuHang);
        if (existsByRank(idGiai, idNoiDung, thuHang))
            throw new IllegalStateException("Thứ hạng đã tồn tại");
        if (existsByVdv(idGiai, idNoiDung, idVdv))
            throw new IllegalStateException("VĐV đã có kết quả ở nội dung này");
        repo.add(new KetQuaCaNhan(idGiai, idNoiDung, idVdv, thuHang));
    }

    // READ
    public KetQuaCaNhan getOne(int idGiai, int idNoiDung, int thuHang) {
        KetQuaCaNhan r = repo.findOne(idGiai, idNoiDung, thuHang);
        if (r == null)
            throw new NoSuchElementException("Không tìm thấy kết quả cá nhân");
        return r;
    }

    public List<KetQuaCaNhan> list(int idGiai, int idNoiDung) {
        return repo.list(idGiai, idNoiDung);
    }

    // UPDATE: thay VĐV cho một thứ hạng
    public void update(int idGiai, int idNoiDung, int thuHang, int idVdv) {
        validate(idVdv, thuHang);
        getOne(idGiai, idNoiDung, thuHang);
        KetQuaCaNhan byVdv = repo.findByVdv(idGiai, idNoiDung, idVdv);
        if (byVdv != null && !byVdv.getThuHang().equals(thuHang))
            throw new IllegalStateException("VĐV đã có kết quả ở thứ hạng khác");
        repo.update(new KetQuaCaNhan(idGiai, idNoiDung, idVdv, thuHang));
    }

    // Đổi thứ hạng
    public void changeRank(int idGiai, int idNoiDung, int oldThuHang, int newThuHang) {
        if (newThuHang <= 0)
            throw new IllegalArgumentException("THU_HANG phải > 0");
        if (existsByRank(idGiai, idNoiDung, newThuHang))
            throw new IllegalStateException("Đã có người ở thứ hạng " + newThuHang);
        repo.updateRank(idGiai, idNoiDung, oldThuHang, newThuHang);
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int thuHang) {
        getOne(idGiai, idNoiDung, thuHang);
        repo.delete(idGiai, idNoiDung, thuHang);
    }

    // Helpers
    public boolean existsByRank(int idGiai, int idNoiDung, int thuHang) {
        return repo.findOne(idGiai, idNoiDung, thuHang) != null;
    }

    public boolean existsByVdv(int idGiai, int idNoiDung, int idVdv) {
        return repo.findByVdv(idGiai, idNoiDung, idVdv) != null;
    }

    private void validate(int idVdv, int thuHang) {
        if (idVdv <= 0)
            throw new IllegalArgumentException("ID_VDV phải > 0");
        if (thuHang <= 0)
            throw new IllegalArgumentException("THU_HANG phải > 0");
    }

    // Bulk replace medals for singles: delete ranks 1,2,3 then add provided rows.
    // This allows two entries for rank=3 (two bronzes).
    public void replaceMedals(int idGiai, int idNoiDung, List<KetQuaCaNhan> items) {
        // delete existing for ranks 1,2,3
        for (int rank : new int[] { 1, 2, 3 }) {
            try {
                repo.delete(idGiai, idNoiDung, rank);
            } catch (RuntimeException ignore) {
            }
        }
        if (items == null)
            return;
        // optional: prevent duplicate VDV within the same replacement batch
        java.util.HashSet<Integer> seenVdv = new java.util.HashSet<>();
        for (KetQuaCaNhan r : items) {
            if (r == null)
                continue;
            validate(r.getIdVdv(), r.getThuHang());
            if (!seenVdv.add(r.getIdVdv()))
                continue; // skip duplicates silently
            repo.add(r);
        }
    }
}
