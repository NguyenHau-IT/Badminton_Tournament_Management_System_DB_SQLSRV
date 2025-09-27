package com.example.btms.service.result;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.repository.result.KetQuaDoiRepository;

public class KetQuaDoiService {
    private final KetQuaDoiRepository repo;

    public KetQuaDoiService(KetQuaDoiRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public void create(int idGiai, int idNoiDung, int idClb, String tenTeam, int thuHang) {
        validate(tenTeam, thuHang);
        if (exists(idGiai, idNoiDung, thuHang)) {
            throw new IllegalStateException("Thứ hạng đã tồn tại");
        }
        repo.add(new KetQuaDoi(idGiai, idNoiDung, idClb, tenTeam.trim(), thuHang));
    }

    // READ
    public KetQuaDoi getOne(int idGiai, int idNoiDung, int thuHang) {
        KetQuaDoi r = repo.findOne(idGiai, idNoiDung, thuHang);
        if (r == null)
            throw new NoSuchElementException("Không tìm thấy kết quả đội");
        return r;
    }

    public List<KetQuaDoi> list(int idGiai, int idNoiDung) {
        return repo.list(idGiai, idNoiDung);
    }

    // UPDATE
    public void update(int idGiai, int idNoiDung, int thuHang, int idClb, String tenTeam) {
        validate(tenTeam, thuHang);
        getOne(idGiai, idNoiDung, thuHang);
        repo.update(new KetQuaDoi(idGiai, idNoiDung, idClb, tenTeam.trim(), thuHang));
    }

    // DELETE
    public void delete(int idGiai, int idNoiDung, int thuHang) {
        getOne(idGiai, idNoiDung, thuHang);
        repo.delete(idGiai, idNoiDung, thuHang);
    }

    // Helpers
    public boolean exists(int idGiai, int idNoiDung, int thuHang) {
        return repo.findOne(idGiai, idNoiDung, thuHang) != null;
    }

    private void validate(String tenTeam, int thuHang) {
        if (tenTeam == null || tenTeam.trim().isEmpty())
            throw new IllegalArgumentException("TEN_TEAM không được rỗng");
        if (thuHang <= 0)
            throw new IllegalArgumentException("THU_HANG phải > 0");
    }

    // Bulk replace medals: delete ranks 1,2,3 and insert provided rows (allows 2
    // bronze)
    public void replaceMedals(int idGiai, int idNoiDung, List<KetQuaDoi> items) {
        // delete existing for ranks 1,2,3
        for (int rank : new int[] { 1, 2, 3 }) {
            try {
                repo.delete(idGiai, idNoiDung, rank);
            } catch (RuntimeException ignore) {
            }
        }
        if (items == null)
            return;
        for (KetQuaDoi r : items) {
            if (r == null)
                continue;
            validate(r.getTenTeam(), r.getThuHang());
            // Assume all belong to same idGiai/idNoiDung passed in
            repo.add(r);
        }
    }
}
