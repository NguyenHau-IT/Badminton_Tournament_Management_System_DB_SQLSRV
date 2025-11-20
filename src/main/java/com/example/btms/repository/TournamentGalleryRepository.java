package com.example.btms.repository;

import com.example.btms.model.gallery.TournamentGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho TournamentGallery Entity
 * 
 * Quản lý hình ảnh, video, documents của giải đấu
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
@Repository
public interface TournamentGalleryRepository extends JpaRepository<TournamentGallery, Integer> {
    
    // ========== DERIVED QUERY METHODS ==========
    
    /**
     * Tìm tất cả media của một giải đấu
     * SQL: SELECT * FROM TOURNAMENT_GALLERY WHERE ID_GIAI = ? ORDER BY THU_TU, THOI_GIAN_TAO
     */
    List<TournamentGallery> findByGiaiDau_IdOrderByThuTuAscThoiGianTaoDesc(Integer id);
    
    /**
     * Tìm media theo giải đấu và loại media
     * SQL: SELECT * FROM TOURNAMENT_GALLERY WHERE ID_GIAI = ? AND LOAI = ? ORDER BY THU_TU
     */
    List<TournamentGallery> findByGiaiDau_IdAndLoaiOrderByThuTuAsc(Integer id, String loai);
    
    /**
     * Tìm tất cả hình ảnh của một giải đấu
     */
    @Query("SELECT g FROM TournamentGallery g WHERE g.giaiDau.id = :id " +
           "AND g.loai = 'image' " +
           "ORDER BY g.thuTu ASC, g.thoiGianTao DESC")
    List<TournamentGallery> findImagesByTournament(@Param("id") Integer id);
    
    /**
     * Tìm tất cả video của một giải đấu
     */
    @Query("SELECT g FROM TournamentGallery g WHERE g.giaiDau.id = :id " +
           "AND g.loai = 'video' " +
           "ORDER BY g.thuTu ASC, g.thoiGianTao DESC")
    List<TournamentGallery> findVideosByTournament(@Param("id") Integer id);
    
    /**
     * Đếm số media của một giải đấu
     */
    @Query("SELECT COUNT(g) FROM TournamentGallery g WHERE g.giaiDau.id = :id")
    long countByTournament(@Param("id") Integer id);
    
    /**
     * Đếm số media theo loại của một giải đấu
     */
    @Query("SELECT COUNT(g) FROM TournamentGallery g WHERE g.giaiDau.id = :id AND g.loai = :loai")
    long countByTournamentAndType(@Param("id") Integer id, @Param("loai") String loai);
    
    /**
     * Xóa tất cả media của một giải đấu
     */
    void deleteByGiaiDau_Id(Integer id);
    
    /**
     * Tìm item có thứ tự lớn nhất (dùng để insert item mới)
     */
    @Query("SELECT COALESCE(MAX(g.thuTu), 0) FROM TournamentGallery g WHERE g.giaiDau.id = :id")
    Integer findMaxThuTu(@Param("id") Integer id);
}
