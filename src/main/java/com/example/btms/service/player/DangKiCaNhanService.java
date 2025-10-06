package com.example.btms.service.player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import com.example.btms.model.player.DangKiCaNhan;
import com.example.btms.repository.player.DangKiCaNhanRepository;

public class DangKiCaNhanService {

    private final DangKiCaNhanRepository repo;

    public DangKiCaNhanService(DangKiCaNhanRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /** Đăng ký mới (mặc định chưa kiểm tra) */
    public void register(int idGiai, int idNoiDung, int idVdv) {
        register(idGiai, idNoiDung, idVdv, LocalDateTime.now(), false);
    }

    /** Đăng ký mới với thời điểm và trạng thái kiểm tra */
    public void register(int idGiai, int idNoiDung, int idVdv,
            LocalDateTime timestamp, boolean kiemTra) {
        if (exists(idGiai, idNoiDung, idVdv)) {
            throw new IllegalStateException("VĐV đã đăng ký rồi");
        }
        repo.add(new DangKiCaNhan(idGiai, idNoiDung, idVdv,
                timestamp != null ? timestamp : LocalDateTime.now(), kiemTra));
    }

    /** Hủy đăng ký */
    public void unregister(int idGiai, int idNoiDung, int idVdv) {
        if (!exists(idGiai, idNoiDung, idVdv)) {
            throw new NoSuchElementException("Không tìm thấy đăng ký để hủy");
        }
        repo.delete(idGiai, idNoiDung, idVdv);
    }

    /** Đánh dấu đã/ chưa kiểm tra */
    public void setKiemTra(int idGiai, int idNoiDung, int idVdv, boolean kiemTra) {
        int n = repo.updateKiemTra(idGiai, idNoiDung, idVdv, kiemTra);
        if (n == 0)
            throw new NoSuchElementException("Không tìm thấy đăng ký để cập nhật");
    }

    /** Kiểm tra tồn tại */
    public boolean exists(int idGiai, int idNoiDung, int idVdv) {
        return repo.findOne(idGiai, idNoiDung, idVdv) != null;
    }

    /** Danh sách theo giải + nội dung, có thể lọc theo KIEM_TRA (null = tất cả) */
    public List<DangKiCaNhan> listByGiaiAndNoiDung(int idGiai, int idNoiDung, Boolean kiemTra) {
        return repo.list(idGiai, idNoiDung, kiemTra);
    }

    public Optional<DangKiCaNhan> findOne(Integer idGiai, Integer idNoiDung, Integer idVdv) {
        DangKiCaNhan r = repo.findOne(idGiai, idNoiDung, idVdv);
        return r != null ? Optional.of(r) : Optional.empty();
    }

    public void delete(Integer idGiai, Integer idNoiDung, Integer idVdv) {
        repo.delete(idGiai, idNoiDung, idVdv);
    }

    /** Xóa tất cả đăng ký cá nhân theo giải */
    public int deleteAllByGiai(int idGiai) {
        return repo.deleteAllByGiai(idGiai);
    }

    /** Xóa tất cả đăng ký cá nhân theo giải và nội dung */
    public int deleteAllByGiaiAndNoiDung(int idGiai, int idNoiDung) {
        return repo.deleteAllByGiaiAndNoiDung(idGiai, idNoiDung);
    }
}
