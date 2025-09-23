package com.example.badmintoneventtechnology.service.cateoftuornament;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.badmintoneventtechnology.model.cateoftuornament.ChiTietGiaiDau;
import com.example.badmintoneventtechnology.repository.cateoftuornament.ChiTietGiaiDauRepository;

/**
 * Service duy nhất cho ChiTietGiaiDau.
 * - Kiểm tra hợp lệ đầu vào (TUOI_DUOI <= TUOI_TREN, >= 0).
 * - Ném lỗi rõ ràng khi bản ghi không tồn tại / đã tồn tại.
 * - Gọn, không phụ thuộc Spring; có thể thêm @Service, @Transactional nếu dùng
 * Spring.
 */
public class ChiTietGiaiDauService {

    private final ChiTietGiaiDauRepository repo;

    public ChiTietGiaiDauService(ChiTietGiaiDauRepository repo) {
        this.repo = Objects.requireNonNull(repo, "repo must not be null");
    }

    /** CREATE */
    public void create(ChiTietGiaiDau chiTiet) {
        validate(chiTiet);
        if (exists(chiTiet.getIdGiaiDau(), chiTiet.getIdNoiDung())) {
            throw new IllegalStateException("Bản ghi đã tồn tại: (ID_GIAI_DAU="
                    + chiTiet.getIdGiaiDau() + ", ID_NOI_DUNG=" + chiTiet.getIdNoiDung() + ")");
        }
        repo.addChiTietGiaiDau(chiTiet);
    }

    /** READ: tất cả */
    public List<ChiTietGiaiDau> findAll() {
        return repo.getAllChiTietGiaiDau();
    }

    /** READ: theo khoá kép */
    public ChiTietGiaiDau findOne(int idGiaiDau, int idNoiDung) {
        ChiTietGiaiDau found = repo.getChiTietGiaiDauById(idGiaiDau, idNoiDung);
        if (found == null) {
            throw notFound(idGiaiDau, idNoiDung);
        }
        return found;
    }

    /** UPDATE */
    public void update(ChiTietGiaiDau chiTiet) {
        validate(chiTiet);
        if (!exists(chiTiet.getIdGiaiDau(), chiTiet.getIdNoiDung())) {
            throw notFound(chiTiet.getIdGiaiDau(), chiTiet.getIdNoiDung());
        }
        repo.updateChiTietGiaiDau(chiTiet);
    }

    /** DELETE */
    public void delete(int idGiaiDau, int idNoiDung) {
        if (!exists(idGiaiDau, idNoiDung)) {
            throw notFound(idGiaiDau, idNoiDung);
        }
        repo.deleteChiTietGiaiDau(idGiaiDau, idNoiDung);
    }

    /** Helper */
    public boolean exists(int idGiaiDau, int idNoiDung) {
        return repo.getChiTietGiaiDauById(idGiaiDau, idNoiDung) != null;
    }

    /* ===================== Private helpers ===================== */

    private void validate(ChiTietGiaiDau c) {
        if (c == null)
            throw new IllegalArgumentException("chiTiet không được null");
        if (c.getTuoiDuoi() < 0 || c.getTuoiTren() < 0)
            throw new IllegalArgumentException("Tuổi không được âm");
        if (c.getTuoiDuoi() > c.getTuoiTren())
            throw new IllegalArgumentException("TUOI_DUOI phải ≤ TUOI_TREN");
    }

    private NoSuchElementException notFound(int idGiaiDau, int idNoiDung) {
        return new NoSuchElementException("Không tìm thấy CHI_TIET_GIAI_DAU với ID_GIAI_DAU="
                + idGiaiDau + ", ID_NOI_DUNG=" + idNoiDung);
    }
}
