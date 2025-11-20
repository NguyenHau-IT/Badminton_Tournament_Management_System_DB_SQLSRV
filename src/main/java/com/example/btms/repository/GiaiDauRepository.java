package com.example.btms.repository;

import com.example.btms.model.tournament.GiaiDau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho GiaiDau Entity
 * 
 * Spring Data JPA tự động implement các methods!
 * 
 * Có 3 loại query methods:
 * 1. Built-in methods (findAll, findById, save, delete, ...)
 * 2. Derived Query Methods (findByTrangThai, findByNoiBat, ...)
 * 3. Custom @Query methods (complex queries với JPQL)
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
@Repository
public interface GiaiDauRepository extends JpaRepository<GiaiDau, Integer>, 
                                           JpaSpecificationExecutor<GiaiDau> {
    
    // ========== BUILT-IN METHODS (từ JpaRepository) ==========
    // Spring đã provide sẵn:
    // - findAll() → List<GiaiDau>
    // - findAll(Pageable) → Page<GiaiDau>
    // - findById(Integer) → Optional<GiaiDau>
    // - save(GiaiDau) → GiaiDau
    // - delete(GiaiDau) → void
    // - count() → long
    // - existsById(Integer) → boolean
    
    // ========== DERIVED QUERY METHODS (Spring tự generate SQL) ==========
    
    /**
     * Tìm tất cả giải đấu theo trạng thái
     * SQL: SELECT * FROM GIAI_DAU WHERE TRANG_THAI = ?
     */
    List<GiaiDau> findByTrangThai(String trangThai);
    
    /**
     * Tìm giải đấu theo trạng thái với pagination
     */
    Page<GiaiDau> findByTrangThai(String trangThai, Pageable pageable);
    
    /**
     * Tìm tất cả giải nổi bật
     * SQL: SELECT * FROM GIAI_DAU WHERE NOI_BAT = 1
     */
    List<GiaiDau> findByNoiBat(Boolean noiBat);
    
    /**
     * Tìm giải nổi bật với pagination
     */
    Page<GiaiDau> findByNoiBat(Boolean noiBat, Pageable pageable);
    
    /**
     * Tìm giải đấu theo tỉnh/thành
     * SQL: SELECT * FROM GIAI_DAU WHERE TINH_THANH = ?
     */
    List<GiaiDau> findByTinhThanh(String tinhThanh);
    
    /**
     * Tìm giải đấu theo tỉnh/thành với pagination
     */
    Page<GiaiDau> findByTinhThanh(String tinhThanh, Pageable pageable);
    
    /**
     * Tìm giải đấu theo cấp độ
     * SQL: SELECT * FROM GIAI_DAU WHERE CAP_DO = ?
     */
    List<GiaiDau> findByCapDo(String capDo);
    
    /**
     * Tìm giải đấu theo thể loại
     * SQL: SELECT * FROM GIAI_DAU WHERE THE_LOAI = ?
     */
    List<GiaiDau> findByTheLoai(String theLoai);
    
    /**
     * Tìm giải đấu theo trạng thái VÀ nổi bật
     * SQL: SELECT * FROM GIAI_DAU WHERE TRANG_THAI = ? AND NOI_BAT = ?
     */
    List<GiaiDau> findByTrangThaiAndNoiBat(String trangThai, Boolean noiBat);
    
    /**
     * Tìm giải đấu theo trạng thái VÀ nổi bật với pagination
     */
    Page<GiaiDau> findByTrangThaiAndNoiBat(String trangThai, Boolean noiBat, Pageable pageable);
    
    /**
     * Tìm giải đấu có ngày bắt đầu sau một ngày cụ thể
     * SQL: SELECT * FROM GIAI_DAU WHERE NGAY_BD >= ?
     */
    List<GiaiDau> findByNgayBdAfter(LocalDate date);
    
    /**
     * Tìm giải đấu có ngày bắt đầu trước một ngày cụ thể
     * SQL: SELECT * FROM GIAI_DAU WHERE NGAY_BD <= ?
     */
    List<GiaiDau> findByNgayBdBefore(LocalDate date);
    
    /**
     * Tìm giải đấu trong khoảng thời gian
     * SQL: SELECT * FROM GIAI_DAU WHERE NGAY_BD BETWEEN ? AND ?
     */
    List<GiaiDau> findByNgayBdBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Tìm giải đấu theo tên (LIKE search)
     * SQL: SELECT * FROM GIAI_DAU WHERE TEN_GIAI LIKE %?%
     */
    List<GiaiDau> findByTenGiaiContainingIgnoreCase(String tenGiai);
    
    /**
     * Tìm giải đấu theo tên với pagination
     */
    Page<GiaiDau> findByTenGiaiContainingIgnoreCase(String tenGiai, Pageable pageable);
    
    // ========== CUSTOM @Query METHODS (JPQL) ==========
    
    /**
     * Tìm giải nổi bật đang mở đăng ký hoặc đang diễn ra
     * Complex query với nhiều điều kiện
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.noiBat = true " +
           "AND (g.trangThai = 'registration' OR g.trangThai = 'ongoing') " +
           "ORDER BY g.ngayBd ASC")
    List<GiaiDau> findActiveFeaturedTournaments();
    
    /**
     * Tìm giải đấu sắp diễn ra (upcoming trong 30 ngày)
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = 'upcoming' " +
           "AND g.ngayBd BETWEEN :startDate AND :endDate " +
           "ORDER BY g.ngayBd ASC")
    List<GiaiDau> findUpcomingTournaments(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Tìm giải đấu sắp diễn ra với pagination
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = 'upcoming' " +
           "AND g.ngayBd BETWEEN :startDate AND :endDate " +
           "ORDER BY g.ngayBd ASC")
    Page<GiaiDau> findUpcomingTournaments(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);
    
    /**
     * Search giải đấu (tên HOẶC địa điểm HOẶC tỉnh/thành)
     */
    @Query("SELECT g FROM GiaiDau g WHERE " +
           "LOWER(g.tenGiai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.diaDiem) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.tinhThanh) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<GiaiDau> searchTournaments(@Param("keyword") String keyword);
    
    /**
     * Search giải đấu với pagination
     */
    @Query("SELECT g FROM GiaiDau g WHERE " +
           "LOWER(g.tenGiai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.diaDiem) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.tinhThanh) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<GiaiDau> searchTournaments(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Tìm giải đấu đang mở đăng ký (registration)
     * Có thể đăng ký ngay hôm nay
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = 'registration' " +
           "AND g.ngayMoDangKi <= :today " +
           "AND g.ngayDongDangKi >= :today " +
           "ORDER BY g.ngayDongDangKi ASC")
    List<GiaiDau> findOpenForRegistration(@Param("today") LocalDate today);
    
    /**
     * Tìm giải đấu đang mở đăng ký với pagination
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = 'registration' " +
           "AND g.ngayMoDangKi <= :today " +
           "AND g.ngayDongDangKi >= :today " +
           "ORDER BY g.ngayDongDangKi ASC")
    Page<GiaiDau> findOpenForRegistration(@Param("today") LocalDate today, Pageable pageable);
    
    /**
     * Tìm giải đấu đang diễn ra (ongoing)
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = 'ongoing' " +
           "AND g.ngayBd <= :today " +
           "AND g.ngayKt >= :today " +
           "ORDER BY g.ngayBd DESC")
    List<GiaiDau> findOngoingTournaments(@Param("today") LocalDate today);
    
    /**
     * Tìm giải đấu theo nhiều trạng thái (IN clause)
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai IN :statuses " +
           "ORDER BY g.ngayBd DESC")
    List<GiaiDau> findByTrangThaiIn(@Param("statuses") List<String> statuses);
    
    /**
     * Tìm giải đấu theo nhiều trạng thái với pagination
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai IN :statuses " +
           "ORDER BY g.ngayBd DESC")
    Page<GiaiDau> findByTrangThaiIn(@Param("statuses") List<String> statuses, Pageable pageable);
    
    /**
     * Tìm top giải đấu có lượt xem cao nhất
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai != 'cancelled' " +
           "ORDER BY g.luotXem DESC")
    List<GiaiDau> findTopViewedTournaments(Pageable pageable);
    
    /**
     * Tìm top giải đấu có đánh giá cao nhất
     */
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai != 'cancelled' " +
           "AND g.danhGiaTb IS NOT NULL " +
           "ORDER BY g.danhGiaTb DESC, g.tongDanhGia DESC")
    List<GiaiDau> findTopRatedTournaments(Pageable pageable);
    
    /**
     * Đếm số giải đấu theo trạng thái
     */
    @Query("SELECT COUNT(g) FROM GiaiDau g WHERE g.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") String trangThai);
    
    /**
     * Đếm số giải nổi bật
     */
    @Query("SELECT COUNT(g) FROM GiaiDau g WHERE g.noiBat = true")
    long countFeaturedTournaments();
    
    /**
     * Tìm giải đấu trong một tháng cụ thể (cho calendar)
     */
    @Query("SELECT g FROM GiaiDau g WHERE " +
           "(YEAR(g.ngayBd) = :year AND MONTH(g.ngayBd) = :month) OR " +
           "(YEAR(g.ngayKt) = :year AND MONTH(g.ngayKt) = :month) OR " +
           "(g.ngayBd <= :monthStart AND g.ngayKt >= :monthEnd) " +
           "ORDER BY g.ngayBd ASC")
    List<GiaiDau> findTournamentsInMonth(@Param("year") int year, 
                                         @Param("month") int month,
                                         @Param("monthStart") LocalDate monthStart,
                                         @Param("monthEnd") LocalDate monthEnd);
    
    /**
     * Advanced search với nhiều filters
     * Dùng cho trang search với multiple criteria
     */
    @Query("SELECT g FROM GiaiDau g WHERE " +
           "(:keyword IS NULL OR " +
           "  LOWER(g.tenGiai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(g.diaDiem) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR g.trangThai = :trangThai) AND " +
           "(:tinhThanh IS NULL OR g.tinhThanh = :tinhThanh) AND " +
           "(:capDo IS NULL OR g.capDo = :capDo) AND " +
           "(:theLoai IS NULL OR g.theLoai = :theLoai) AND " +
           "(:noiBat IS NULL OR g.noiBat = :noiBat) AND " +
           "(:fromDate IS NULL OR g.ngayBd >= :fromDate) AND " +
           "(:toDate IS NULL OR g.ngayBd <= :toDate)")
    Page<GiaiDau> advancedSearch(@Param("keyword") String keyword,
                                 @Param("trangThai") String trangThai,
                                 @Param("tinhThanh") String tinhThanh,
                                 @Param("capDo") String capDo,
                                 @Param("theLoai") String theLoai,
                                 @Param("noiBat") Boolean noiBat,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate,
                                 Pageable pageable);
    
    // ========== AUTOCOMPLETE SEARCH ==========
    
    /**
     * Tìm kiếm giải đấu theo keyword cho autocomplete
     * Tìm trong: tên giải, tỉnh thành, mô tả
     * Sắp xếp theo: trạng thái (ongoing > registration > upcoming > completed) + ngày mới nhất
     * 
     * Use case: Search box autocomplete dropdown
     * Dùng native SQL để support TOP clause của SQL Server
     */
    @Query(value = "SELECT TOP (:limit) * FROM GIAI_DAU " +
                   "WHERE LOWER(TEN_GIAI) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "   OR LOWER(TINH_THANH) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "   OR LOWER(MO_TA) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "ORDER BY " +
                   "  CASE TRANG_THAI " +
                   "    WHEN 'ongoing' THEN 1 " +
                   "    WHEN 'registration' THEN 2 " +
                   "    WHEN 'upcoming' THEN 3 " +
                   "    ELSE 4 " +
                   "  END, " +
                   "  NGAY_BD DESC", 
           nativeQuery = true)
    List<GiaiDau> findByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
    
    // ========== SPECIFICATION EXECUTOR ==========
    // JpaSpecificationExecutor<GiaiDau> provides:
    // - findAll(Specification<GiaiDau>) → List<GiaiDau>
    // - findOne(Specification<GiaiDau>) → Optional<GiaiDau>
    // - count(Specification<GiaiDau>) → long
    // 
    // Dùng cho dynamic queries phức tạp (sẽ implement trong Service)
}
