package com.example.badmintoneventtechnology.service.player;

import com.example.badmintoneventtechnology.model.player.VanDongVien;
import com.example.badmintoneventtechnology.repository.player.VanDongVienRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class VanDongVienService {

    private final VanDongVienRepository repo;

    public VanDongVienService(VanDongVienRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    // CREATE
    public VanDongVien create(String hoTen, LocalDate ngaySinh, Integer idClb, String gioiTinh) {
        validate(hoTen, gioiTinh);
        return repo.add(new VanDongVien(hoTen, ngaySinh, idClb, gioiTinh));
    }

    // READ
    public List<VanDongVien> findAll() {
        return repo.findAll();
    }

    public VanDongVien findOne(int id) {
        VanDongVien vdv = repo.findById(id);
        if (vdv == null)
            throw notFound(id);
        return vdv;
    }

    // UPDATE
    public void update(int id, String hoTen, LocalDate ngaySinh, Integer idClb, String gioiTinh) {
        validate(hoTen, gioiTinh);
        VanDongVien current = findOne(id);
        current.setHoTen(hoTen);
        current.setNgaySinh(ngaySinh);
        current.setIdClb(idClb);
        current.setGioiTinh(gioiTinh);
        repo.update(current);
    }

    // DELETE
    public void delete(int id) {
        findOne(id); // đảm bảo tồn tại
        repo.delete(id);
    }

    /* ========== private helpers ========== */
    private void validate(String hoTen, String gioiTinh) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được rỗng");
        }
        if (gioiTinh != null && !"MFO".contains(gioiTinh.toUpperCase())) {
            throw new IllegalArgumentException("Giới tính chỉ được phép 'M', 'F' hoặc 'O'");
        }
    }

    private NoSuchElementException notFound(int id) {
        return new NoSuchElementException("Không tìm thấy VAN_DONG_VIEN với ID=" + id);
    }
}
