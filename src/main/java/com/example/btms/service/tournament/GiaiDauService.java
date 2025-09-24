package com.example.btms.service.tournament;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.repository.tuornament.GiaiDauRepository;
import com.example.btms.service.db.DatabaseService;

/**
 * Service class để xử lý business logic cho GiaiDau
 */
public class GiaiDauService {
    private final DatabaseService databaseService;
    private GiaiDauRepository repository;

    public GiaiDauService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        initializeRepository();
    }

    /**
     * Khởi tạo repository với connection hiện tại
     */
    private void initializeRepository() {
        Connection connection = databaseService.current();
        if (connection != null) {
            this.repository = new GiaiDauRepository(connection);
        }
    }

    /**
     * Đảm bảo repository được khởi tạo
     */
    private void ensureRepository() throws SQLException {
        if (repository == null) {
            Connection connection = databaseService.current();
            if (connection == null) {
                throw new SQLException("No database connection available");
            }
            repository = new GiaiDauRepository(connection);
        }
    }

    /**
     * Tạo giải đấu mới
     */
    public GiaiDau createGiaiDau(String tenGiai, LocalDate ngayBd, LocalDate ngayKt, Integer idUser)
            throws SQLException {
        ensureRepository();

        // Validation
        if (tenGiai == null || tenGiai.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên giải đấu không được để trống");
        }

        if (ngayBd != null && ngayKt != null && ngayBd.isAfter(ngayKt)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        if (idUser == null) {
            throw new IllegalArgumentException("ID User không được để trống");
        }

        GiaiDau giaiDau = new GiaiDau(tenGiai.trim(), ngayBd, ngayKt, idUser);
        return repository.create(giaiDau);
    }

    /**
     * Lấy tất cả giải đấu
     */
    public List<GiaiDau> getAllGiaiDau() throws SQLException {
        ensureRepository();
        return repository.findAll();
    }

    /**
     * Lấy giải đấu theo ID
     */
    public Optional<GiaiDau> getGiaiDauById(Integer id) throws SQLException {
        ensureRepository();
        return repository.findById(id);
    }

    /**
     * Tìm kiếm giải đấu theo tên
     */
    public List<GiaiDau> searchGiaiDauByName(String tenGiai) throws SQLException {
        ensureRepository();
        return repository.findByName(tenGiai);
    }

    /**
     * Lấy giải đấu theo User ID
     */
    public List<GiaiDau> getGiaiDauByUserId(Long userId) throws SQLException {
        ensureRepository();
        return repository.findByUserId(userId);
    }

    /**
     * Cập nhật giải đấu
     */
    public boolean updateGiaiDau(GiaiDau giaiDau) throws SQLException {
        ensureRepository();

        // Validation
        if (giaiDau.getId() == null) {
            throw new IllegalArgumentException("ID giải đấu không được để trống");
        }

        if (giaiDau.getTenGiai() == null || giaiDau.getTenGiai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên giải đấu không được để trống");
        }

        if (giaiDau.getNgayBd() != null && giaiDau.getNgayKt() != null &&
                giaiDau.getNgayBd().isAfter(giaiDau.getNgayKt())) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        if (giaiDau.getIdUser() == null) {
            throw new IllegalArgumentException("ID User không được để trống");
        }

        // Cập nhật timestamp
        giaiDau.updatestamp();

        return repository.update(giaiDau);
    }

    /**
     * Xóa giải đấu
     */
    public boolean deleteGiaiDau(Integer id) throws SQLException {
        ensureRepository();

        if (id == null) {
            throw new IllegalArgumentException("ID giải đấu không được để trống");
        }

        // Kiểm tra giải đấu có tồn tại không
        if (!repository.exists(id)) {
            throw new IllegalArgumentException("Giải đấu không tồn tại");
        }

        return repository.delete(id);
    }

    /**
     * Đếm số lượng giải đấu
     */
    public Integer countGiaiDau() throws SQLException {
        ensureRepository();
        return repository.count();
    }

    /**
     * Kiểm tra giải đấu có tồn tại không
     */
    public boolean existsGiaiDau(Integer id) throws SQLException {
        ensureRepository();
        return repository.exists(id);
    }

    /**
     * Lấy giải đấu đang hoạt động (hiện tại)
     */
    public List<GiaiDau> getActiveGiaiDau() throws SQLException {
        ensureRepository();
        List<GiaiDau> allGiaiDau = repository.findAll();
        LocalDate now = LocalDate.now();

        return allGiaiDau.stream()
                .filter(giaiDau -> giaiDau.getNgayBd() != null && giaiDau.getNgayKt() != null)
                .filter(giaiDau -> now.isAfter(giaiDau.getNgayBd()) && now.isBefore(giaiDau.getNgayKt()))
                .toList();
    }

    /**
     * Lấy giải đấu sắp tới
     */
    public List<GiaiDau> getUpcomingGiaiDau() throws SQLException {
        ensureRepository();
        List<GiaiDau> allGiaiDau = repository.findAll();
        LocalDate now = LocalDate.now();

        return allGiaiDau.stream()
                .filter(giaiDau -> giaiDau.getNgayBd() != null)
                .filter(giaiDau -> now.isBefore(giaiDau.getNgayBd()))
                .toList();
    }

    /**
     * Lấy giải đấu đã kết thúc
     */
    public List<GiaiDau> getFinishedGiaiDau() throws SQLException {
        ensureRepository();
        List<GiaiDau> allGiaiDau = repository.findAll();
        LocalDate now = LocalDate.now();

        return allGiaiDau.stream()
                .filter(giaiDau -> giaiDau.getNgayKt() != null)
                .filter(giaiDau -> now.isAfter(giaiDau.getNgayKt()))
                .toList();
    }

    /**
     * Refresh repository khi connection thay đổi
     */
    public void refreshRepository() {
        initializeRepository();
    }
}
