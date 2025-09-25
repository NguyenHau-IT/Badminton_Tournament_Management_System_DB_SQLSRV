package com.example.btms.service.player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.example.btms.model.player.DangKyCaNhan;
import com.example.btms.repository.player.DangKyCaNhanRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.category.NoiDung;

/**
 * Service nghiệp vụ đăng ký cá nhân: đảm bảo
 * - Không trùng (giải, nội dung, VĐV)
 * - Nội dung phải là nội dung đơn (TEAM = false)
 * - VĐV tồn tại
 */
public class DangKyCaNhanService {
    private final DangKyCaNhanRepository repo;
    private final VanDongVienRepository vdvRepo;
    private final NoiDungRepository ndRepo;

    public DangKyCaNhanService(DangKyCaNhanRepository repo, VanDongVienRepository vdvRepo, NoiDungRepository ndRepo) {
        this.repo = Objects.requireNonNull(repo);
        this.vdvRepo = Objects.requireNonNull(vdvRepo);
        this.ndRepo = Objects.requireNonNull(ndRepo);
    }

    public DangKyCaNhan create(int idGiai, int idNoiDung, int idVdv) {
        validate(idGiai, idNoiDung, idVdv, true);
        return repo.add(new DangKyCaNhan(idGiai, idNoiDung, idVdv));
    }

    public void update(int id, int idGiai, int idNoiDung, int idVdv) {
        DangKyCaNhan current = require(id);
        validate(idGiai, idNoiDung, idVdv, !sameKey(current, idGiai, idNoiDung, idVdv));
        current.setIdGiai(idGiai);
        current.setIdNoiDung(idNoiDung);
        current.setIdVdv(idVdv);
        repo.update(current);
    }

    public void delete(int id) { require(id); repo.delete(id); }

    public DangKyCaNhan get(int id) { return require(id); }

    public List<DangKyCaNhan> listByGiai(int idGiai) { return repo.findAllByGiai(idGiai); }

    public List<DangKyCaNhan> listByGiaiAndNoiDung(int idGiai, int idNoiDung) { return repo.findAllByGiaiAndNoiDung(idGiai, idNoiDung); }

    /* ================= helpers ================= */
    private DangKyCaNhan require(int id) {
        DangKyCaNhan d = repo.findById(id);
        if (d == null) throw new NoSuchElementException("Không tìm thấy đăng ký cá nhân ID=" + id);
        return d;
    }

    private boolean sameKey(DangKyCaNhan d, int idGiai, int idNoiDung, int idVdv) {
        return d.getIdGiai() == idGiai && d.getIdNoiDung() == idNoiDung && d.getIdVdv() == idVdv;
    }

    private void validate(int idGiai, int idNoiDung, int idVdv, boolean checkDup) {
        if (idGiai <= 0) throw new IllegalArgumentException("ID_GIAI không hợp lệ");
        java.util.Optional<NoiDung> ndOpt;
        try {
            ndOpt = ndRepo.findById(idNoiDung); // repository hiện trả về Optional & throws SQLException
        } catch (Exception ex) {
            throw new IllegalArgumentException("Lỗi tra cứu nội dung: " + ex.getMessage(), ex);
        }
        if (ndOpt == null || ndOpt.isEmpty()) throw new IllegalArgumentException("Nội dung không tồn tại");
        NoiDung nd = ndOpt.get();
    if (Boolean.TRUE.equals(nd.getTeam())) throw new IllegalArgumentException("Nội dung này là đôi, không phải đơn");
        VanDongVien v = vdvRepo.findById(idVdv);
        if (v == null) throw new IllegalArgumentException("Vận động viên không tồn tại");
        if (checkDup && repo.exists(idGiai, idNoiDung, idVdv)) throw new IllegalStateException("Đã đăng ký nội dung này");
    }
}
