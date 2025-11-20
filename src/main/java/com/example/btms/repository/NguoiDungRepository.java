package com.example.btms.repository;

import com.example.btms.model.user.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho NguoiDung Entity
 * 
 * Quản lý user authentication, authorization, và user management
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    
    // ========== AUTHENTICATION & AUTHORIZATION ==========
    
    /**
     * Tìm user theo email (đăng nhập bằng email)
     * SQL: SELECT * FROM NGUOI_DUNG WHERE EMAIL = ?
     */
    Optional<NguoiDung> findByEmail(String email);
    
    /**
     * Kiểm tra email đã tồn tại chưa (validation)
     */
    boolean existsByEmail(String email);
    
    /**
     * Tìm user theo vai trò
     * SQL: SELECT * FROM NGUOI_DUNG WHERE VAI_TRO = ?
     */
    List<NguoiDung> findByVaiTro(String vaiTro);
    
    /**
     * Tìm user theo vai trò với pagination
     */
    Page<NguoiDung> findByVaiTro(String vaiTro, Pageable pageable);
    
    // ========== USER STATUS ==========
    
    /**
     * Tìm user theo trạng thái
     * SQL: SELECT * FROM NGUOI_DUNG WHERE TRANG_THAI = ?
     */
    List<NguoiDung> findByTrangThai(String trangThai);
    
    /**
     * Tìm user theo trạng thái với pagination
     */
    Page<NguoiDung> findByTrangThai(String trangThai, Pageable pageable);
    
    /**
     * Tìm tất cả user đang active
     */
    @Query("SELECT u FROM NguoiDung u WHERE u.trangThai = 'active' ORDER BY u.thoiGianTao DESC")
    List<NguoiDung> findAllActiveUsers();
    
    /**
     * Tìm user cần verify (pending_verification)
     */
    @Query("SELECT u FROM NguoiDung u WHERE u.trangThai = 'pending_verification' ORDER BY u.thoiGianTao ASC")
    List<NguoiDung> findPendingVerificationUsers();
    
    // ========== SEARCH ==========
    
    /**
     * Search user theo tên (họ tên)
     */
    @Query("SELECT u FROM NguoiDung u WHERE LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NguoiDung> searchByName(@Param("keyword") String keyword);
    
    /**
     * Search user với pagination
     */
    @Query("SELECT u FROM NguoiDung u WHERE " +
           "LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<NguoiDung> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Advanced search với nhiều filters
     */
    @Query("SELECT u FROM NguoiDung u WHERE " +
           "(:keyword IS NULL OR " +
           "  LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:vaiTro IS NULL OR u.vaiTro = :vaiTro) AND " +
           "(:trangThai IS NULL OR u.trangThai = :trangThai)")
    Page<NguoiDung> advancedSearch(@Param("keyword") String keyword,
                                   @Param("vaiTro") String vaiTro,
                                   @Param("trangThai") String trangThai,
                                   Pageable pageable);
    
    // ========== STATISTICS ==========
    
    /**
     * Đếm số user theo vai trò
     */
    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.vaiTro = :vaiTro")
    long countByVaiTro(@Param("vaiTro") String vaiTro);
    
    /**
     * Đếm số user theo trạng thái
     */
    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") String trangThai);
    
    /**
     * Đếm tổng số admin
     */
    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.vaiTro = 'admin'")
    long countAdmins();
    
    /**
     * Đếm tổng số organizer
     */
    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.vaiTro = 'organizer'")
    long countOrganizers();
    
    /**
     * Đếm tổng số player
     */
    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.vaiTro = 'player'")
    long countPlayers();
}
