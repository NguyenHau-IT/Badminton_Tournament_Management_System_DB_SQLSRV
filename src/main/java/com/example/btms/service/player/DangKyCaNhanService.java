package com.example.btms.service.player;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.btms.model.player.DangKyCaNhan;
import com.example.btms.repository.player.DangKyCaNhanRepository;

/**
 * Service quản lý đăng ký cá nhân (Singles).
 */
public class DangKyCaNhanService {
    private final DangKyCaNhanRepository repo;

    public DangKyCaNhanService(Connection conn) {
        this.repo = new DangKyCaNhanRepository(conn);
    }

    public DangKyCaNhan create(int idGiai, int idNoiDung, int idVdv) {
        validateIds(idGiai, idNoiDung, idVdv);
        if (repo.exists(idGiai, idNoiDung, idVdv)) {
            throw new IllegalStateException("VĐV đã đăng ký nội dung này trong giải");
        }
        return repo.add(new DangKyCaNhan(idGiai, idNoiDung, idVdv));
    }

    public void update(DangKyCaNhan d) {
        Objects.requireNonNull(d, "DangKyCaNhan null");
        validateIds(d.getIdGiai(), d.getIdNoiDung(), d.getIdVdv());
        // Kiểm tra trùng (ngoại trừ chính bản ghi hiện tại)
        if (repo.exists(d.getIdGiai(), d.getIdNoiDung(), d.getIdVdv())) {
            // Nếu cần tinh chỉnh để loại bỏ chính nó thì phải truy vấn ID hiện tại; giản lược.
            // Người dùng sẽ không được phép chuyển thành bản ghi giống hệt bản ghi khác.
            Optional<DangKyCaNhan> current = repo.findById(d.getId());
            if (current.isEmpty() || !sameTriple(current.get(), d)) {
                throw new IllegalStateException("Trùng đăng ký");
            }
        }
        repo.update(d);
    }

    private boolean sameTriple(DangKyCaNhan a, DangKyCaNhan b) {
        return a.getIdGiai() == b.getIdGiai() && a.getIdNoiDung() == b.getIdNoiDung() && a.getIdVdv() == b.getIdVdv();
    }

    public void delete(int id) { repo.delete(id); }

    public List<DangKyCaNhan> listByGiai(int idGiai) { return repo.findAllByGiai(idGiai); }

    public List<DangKyCaNhan> listByGiaiAndNoiDung(int idGiai, int idNoiDung) { return repo.findAllByGiaiAndNoiDung(idGiai, idNoiDung); }

    public Optional<DangKyCaNhan> find(int id) { return repo.findById(id); }

    private void validateIds(int idGiai, int idNoiDung, int idVdv) {
        if (idGiai <= 0 || idNoiDung <= 0 || idVdv <= 0) {
            throw new IllegalArgumentException("ID không hợp lệ");
        }
    }
}
