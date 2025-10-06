package com.example.btms.service.match;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import com.example.btms.model.match.ChiTietTranDau;
import com.example.btms.repository.match.ChiTietTranDauRepository;
import com.example.btms.util.uuid.UuidV7;

public class ChiTietTranDauService {
    private final ChiTietTranDauRepository repo;

    public ChiTietTranDauService(ChiTietTranDauRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /** Tạo trận: nếu id null → tự sinh UUID */
    public String create(String id, int theThuc, int idVdvThang,
            LocalDateTime batDau, LocalDateTime ketThuc, int san) {
        if (id == null || id.isBlank())
            id = UUID.randomUUID().toString();
        validateTimes(batDau, ketThuc);
        repo.add(new ChiTietTranDau(id, theThuc, idVdvThang, batDau, ketThuc, san));
        return id;
    }

    /** Tạo trận với UUID v7 (time-ordered) làm ID. */
    public String createV7(LocalDateTime batDau, int theThuc, int san) {
        String id = UuidV7.generate();
        // ID_VDV_THANG chưa biết khi bắt đầu (0 nghĩa là chưa có)
        int idVdvThang = 0;
        LocalDateTime ketThuc = batDau; // placeholder, sẽ update khi kết thúc
        repo.add(new ChiTietTranDau(id, theThuc, idVdvThang, batDau, ketThuc, san));
        return id;
    }

    public ChiTietTranDau get(String id) {
        ChiTietTranDau m = repo.findById(id);
        if (m == null)
            throw new NoSuchElementException("Không tìm thấy trận");
        return m;
    }

    public List<ChiTietTranDau> listAll() {
        return repo.listAll();
    }

    public void update(String id, int theThuc, int idVdvThang,
            LocalDateTime batDau, LocalDateTime ketThuc, int san) {
        validateTimes(batDau, ketThuc);
        get(id); // ensure exists
        repo.update(new ChiTietTranDau(id, theThuc, idVdvThang, batDau, ketThuc, san));
    }

    public void delete(String id) {
        get(id);
        repo.delete(id);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Thời gian không được null");
        if (end.isBefore(start))
            throw new IllegalArgumentException("KET_THUC phải >= BAT_DAU");
    }
}
