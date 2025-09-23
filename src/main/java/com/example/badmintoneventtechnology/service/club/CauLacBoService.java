package com.example.badmintoneventtechnology.service.club;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.badmintoneventtechnology.model.club.CauLacBo;
import com.example.badmintoneventtechnology.repository.club.CauLacBoRepository;

public class CauLacBoService {

    private final CauLacBoRepository repo;

    public CauLacBoService(CauLacBoRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public CauLacBo create(String tenClb, String tenNgan) {
        validateTen(tenClb, tenNgan);
        ensureNameNotExists(tenClb);
        return repo.add(new CauLacBo(tenClb.trim(), tenNgan != null ? tenNgan.trim() : null));
    }

    // READ
    public List<CauLacBo> findAll() {
        return repo.findAll();
    }

    public CauLacBo findOne(int id) {
        CauLacBo clb = repo.findById(id);
        if (clb == null)
            throw notFound(id);
        return clb;
    }

    // UPDATE
    public void update(int id, String tenClb, String tenNgan) {
        validateTen(tenClb, tenNgan);
        CauLacBo current = findOne(id);
        // check duplicate
        CauLacBo byName = repo.findByTen(tenClb.trim());
        if (byName != null && !byName.getId().equals(id)) {
            throw new IllegalStateException("Tên câu lạc bộ đã tồn tại: " + tenClb);
        }
        current.setTenClb(tenClb.trim());
        current.setTenNgan(tenNgan != null ? tenNgan.trim() : null);
        repo.update(current);
    }

    // DELETE
    public void delete(int id) {
        findOne(id); // đảm bảo tồn tại
        repo.delete(id);
    }

    // Helpers
    public boolean existsById(int id) {
        return repo.findById(id) != null;
    }

    /* ========== private ========== */
    private void validateTen(String tenClb, String tenNgan) {
        if (tenClb == null || tenClb.trim().isEmpty()) {
            throw new IllegalArgumentException("TEN_CLB không được rỗng");
        }
        if (tenClb.trim().length() > 255) {
            throw new IllegalArgumentException("TEN_CLB tối đa 255 ký tự");
        }
        if (tenNgan != null && tenNgan.trim().length() > 100) {
            throw new IllegalArgumentException("TEN_NGAN tối đa 100 ký tự");
        }
    }

    private void ensureNameNotExists(String tenClb) {
        if (repo.findByTen(tenClb.trim()) != null) {
            throw new IllegalStateException("Tên câu lạc bộ đã tồn tại: " + tenClb);
        }
    }

    private NoSuchElementException notFound(int id) {
        return new NoSuchElementException("Không tìm thấy CAU_LAC_BO với ID=" + id);
    }
}
