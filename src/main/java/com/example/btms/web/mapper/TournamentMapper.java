package com.example.btms.web.mapper;

import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.model.gallery.TournamentGallery;
import com.example.btms.web.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper để convert giữa Entity (GiaiDau) và DTOs
 * 
 * Chức năng:
 * - Entity → DTO (cho API response)
 * - DTO → Entity (cho create/update)
 * 
 * Pattern: Stateless component, có thể inject vào Service
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
@Component
public class TournamentMapper {
    
    // ========== ENTITY → TournamentDTO (List View) ==========
    
    /**
     * Convert GiaiDau Entity → TournamentDTO
     * Dùng cho danh sách giải đấu, search results
     */
    public TournamentDTO toDTO(GiaiDau entity) {
        if (entity == null) {
            return null;
        }
        
        TournamentDTO dto = new TournamentDTO();
        
        // Basic info
        dto.setId(entity.getId());
        dto.setTenGiai(entity.getTenGiai());
        dto.setDiaDiem(entity.getDiaDiem());
        dto.setTinhThanh(entity.getTinhThanh());
        dto.setQuocGia(entity.getQuocGia());
        
        // Dates (Entity uses ngayBd/ngayKt, DTO uses ngayBatDau/ngayKetThuc)
        dto.setNgayBatDau(entity.getNgayBd());
        dto.setNgayKetThuc(entity.getNgayKt());
        dto.setNgayMoDangKi(entity.getNgayMoDangKi());
        dto.setNgayDongDangKi(entity.getNgayDongDangKi());
        
        // Status & Category
        dto.setTrangThai(entity.getTrangThai());
        dto.setCapDo(entity.getCapDo());
        dto.setTheLoai(entity.getTheLoai());
        dto.setNoiBat(entity.getNoiBat());
        
        // Media
        dto.setHinhAnh(entity.getHinhAnh());
        dto.setLogo(entity.getLogo());
        
        // Metrics
        dto.setLuotXem(entity.getLuotXem());
        dto.setDanhGiaTb(entity.getDanhGiaTb());
        dto.setTongDanhGia(entity.getTongDanhGia());
        
        // Registration
        dto.setSoLuongToiDa(entity.getSoLuongToiDa());
        dto.setPhiThamGia(entity.getPhiThamGia());
        
        return dto;
    }
    
    /**
     * Convert List<GiaiDau> → List<TournamentDTO>
     */
    public List<TournamentDTO> toDTOList(List<GiaiDau> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // ========== ENTITY → TournamentDetailDTO (Detail View) ==========
    
    /**
     * Convert GiaiDau Entity → TournamentDetailDTO
     * Dùng cho trang chi tiết giải đấu (đầy đủ thông tin)
     */
    public TournamentDetailDTO toDetailDTO(GiaiDau entity) {
        if (entity == null) {
            return null;
        }
        
        TournamentDetailDTO dto = new TournamentDetailDTO();
        
        // Basic info (giống TournamentDTO)
        dto.setId(entity.getId());
        dto.setTenGiai(entity.getTenGiai());
        dto.setDiaDiem(entity.getDiaDiem());
        dto.setTinhThanh(entity.getTinhThanh());
        dto.setQuocGia(entity.getQuocGia());
        
        // Dates (Entity uses ngayBd/ngayKt)
        dto.setNgayBatDau(entity.getNgayBd());
        dto.setNgayKetThuc(entity.getNgayKt());
        dto.setNgayMoDangKi(entity.getNgayMoDangKi());
        dto.setNgayDongDangKi(entity.getNgayDongDangKi());
        
        // Status & Category
        dto.setTrangThai(entity.getTrangThai());
        dto.setCapDo(entity.getCapDo());
        dto.setTheLoai(entity.getTheLoai());
        dto.setNoiBat(entity.getNoiBat());
        
        // FULL DESCRIPTION (khác với TournamentDTO)
        dto.setMoTa(entity.getMoTa());
        dto.setQuyDinh(entity.getQuyDinh());
        dto.setGiaiThuong(entity.getGiaiThuong());
        dto.setSanThiDau(entity.getSanThiDau());
        
        // CONTACT INFO (khác với TournamentDTO)
        dto.setDienThoai(entity.getDienThoai());
        dto.setEmail(entity.getEmail());
        dto.setWebsite(entity.getWebsite());
        
        // Media
        dto.setHinhAnh(entity.getHinhAnh());
        dto.setLogo(entity.getLogo());
        
        // Metrics
        dto.setLuotXem(entity.getLuotXem());
        dto.setDanhGiaTb(entity.getDanhGiaTb());
        dto.setTongDanhGia(entity.getTongDanhGia());
        
        // Registration
        dto.setSoLuongToiDa(entity.getSoLuongToiDa());
        dto.setPhiThamGia(entity.getPhiThamGia());
        // TODO: Set soLuongDaDangKy từ Registration service sau
        
        // GALLERY - sẽ thêm sau khi có relationship
        // dto.setGallery(toGalleryItemDTOList(entity.getGalleryItems()));
        
        return dto;
    }
    
    /**
     * Convert TournamentGallery → GalleryItemDTO
     * Helper method cho DetailDTO
     */
    public TournamentDetailDTO.GalleryItemDTO toGalleryItemDTO(TournamentGallery gallery) {
        if (gallery == null) {
            return null;
        }
        
        TournamentDetailDTO.GalleryItemDTO dto = new TournamentDetailDTO.GalleryItemDTO();
        dto.setId(gallery.getId());
        dto.setLoai(gallery.getLoai());
        dto.setUrl(gallery.getUrl());
        dto.setTieuDe(gallery.getTieuDe());
        dto.setMoTa(gallery.getMoTa());
        dto.setThuTu(gallery.getThuTu());
        
        return dto;
    }
    
    /**
     * Convert List<TournamentGallery> → List<GalleryItemDTO>
     */
    public List<TournamentDetailDTO.GalleryItemDTO> toGalleryItemDTOList(List<TournamentGallery> galleries) {
        if (galleries == null) {
            return new ArrayList<>();
        }
        return galleries.stream()
                .map(this::toGalleryItemDTO)
                .collect(Collectors.toList());
    }
    
    // ========== ENTITY → TournamentCardDTO (Compact View) ==========
    
    /**
     * Convert GiaiDau Entity → TournamentCardDTO
     * Dùng cho cards nhỏ (homepage, sidebar, mobile)
     */
    public TournamentCardDTO toCardDTO(GiaiDau entity) {
        if (entity == null) {
            return null;
        }
        
        TournamentCardDTO dto = new TournamentCardDTO();
        
        // Minimal info
        dto.setId(entity.getId());
        dto.setTenGiai(entity.getTenGiai());
        dto.setHinhAnh(entity.getHinhAnh());
        dto.setTinhThanh(entity.getTinhThanh());
        
        // Dates (Entity uses ngayBd/ngayKt)
        dto.setNgayBatDau(entity.getNgayBd());
        dto.setNgayKetThuc(entity.getNgayKt());
        
        // Status & badges
        dto.setTrangThai(entity.getTrangThai());
        dto.setNoiBat(entity.getNoiBat());
        
        // Metrics (chỉ 2 số quan trọng)
        dto.setDanhGiaTb(entity.getDanhGiaTb());
        dto.setLuotXem(entity.getLuotXem());
        
        // Quick info
        dto.setCapDo(entity.getCapDo());
        dto.setPhiThamGia(entity.getPhiThamGia());
        
        return dto;
    }
    
    /**
     * Convert List<GiaiDau> → List<TournamentCardDTO>
     */
    public List<TournamentCardDTO> toCardDTOList(List<GiaiDau> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toCardDTO)
                .collect(Collectors.toList());
    }
    
    // ========== ENTITY → TournamentCalendarEventDTO (Calendar View) ==========
    
    /**
     * Convert GiaiDau Entity → TournamentCalendarEventDTO
     * Dùng cho FullCalendar.js
     */
    public TournamentCalendarEventDTO toCalendarEventDTO(GiaiDau entity) {
        if (entity == null) {
            return null;
        }
        
        TournamentCalendarEventDTO dto = new TournamentCalendarEventDTO(
                entity.getId(),
                entity.getTenGiai(),
                entity.getNgayBd(),      // Entity uses ngayBd
                entity.getNgayKt(),      // Entity uses ngayKt
                entity.getTrangThai(),
                entity.getTinhThanh()
        );
        
        // Extended props
        dto.setDescription(truncateText(entity.getMoTa(), 100)); // Mô tả ngắn cho tooltip
        dto.setLevel(entity.getCapDo());
        dto.setFeatured(entity.getNoiBat());
        dto.setImageUrl(entity.getHinhAnh());
        
        return dto;
    }
    
    /**
     * Convert List<GiaiDau> → List<TournamentCalendarEventDTO>
     */
    public List<TournamentCalendarEventDTO> toCalendarEventDTOList(List<GiaiDau> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toCalendarEventDTO)
                .collect(Collectors.toList());
    }
    
    // ========== ENTITY → TournamentStatsDTO (Statistics View) ==========
    
    /**
     * Convert GiaiDau Entity → TournamentStatsDTO
     * Dùng cho analytics, admin dashboard
     * 
     * NOTE: Một số metrics cần tính toán từ các service khác:
     * - luotXemHomNay, luotXemTuanNay, luotXemThangNay (từ ViewLog)
     * - soLuongDaDangKy, soLuongDaDuyet (từ Registration)
     * - rating breakdown (từ Rating)
     * - trend data (từ Analytics)
     * 
     * Mapper chỉ set basic fields, các service sẽ enrich thêm
     */
    public TournamentStatsDTO toStatsDTO(GiaiDau entity) {
        if (entity == null) {
            return null;
        }
        
        TournamentStatsDTO dto = new TournamentStatsDTO(
                entity.getId(),
                entity.getTenGiai(),
                entity.getTrangThai()
        );
        
        // Basic metrics từ Entity
        dto.setLuotXem(entity.getLuotXem());
        dto.setDanhGiaTb(entity.getDanhGiaTb());
        dto.setTongDanhGia(entity.getTongDanhGia());
        
        // Registration info
        dto.setSoLuongToiDa(entity.getSoLuongToiDa());
        dto.setPhiThamGia(entity.getPhiThamGia());
        
        // Dates (Entity uses ngayBd/ngayKt)
        dto.setNgayBatDau(entity.getNgayBd());
        dto.setNgayKetThuc(entity.getNgayKt());
        dto.setNgayMoDangKi(entity.getNgayMoDangKi());
        dto.setNgayDongDangKi(entity.getNgayDongDangKi());
        
        // Calculate time metrics
        LocalDate today = LocalDate.now();
        if (entity.getNgayBd() != null) {
            dto.setSoNgayConLai((int) ChronoUnit.DAYS.between(today, entity.getNgayBd()));
        }
        if (entity.getNgayDongDangKi() != null) {
            dto.setSoNgayDangKyConLai((int) ChronoUnit.DAYS.between(today, entity.getNgayDongDangKi()));
        }
        
        // Advanced metrics sẽ được set bởi Service layer
        // - dto.setLuotXemHomNay() - from ViewLogService
        // - dto.setSoLuongDaDangKy() - from RegistrationService
        // - dto.setRating5Stars() - from RatingService
        // - dto.setViewsByDate() - from AnalyticsService
        
        return dto;
    }
    
    // ========== DTO → ENTITY (Create/Update) ==========
    
    /**
     * Convert TournamentDTO → GiaiDau Entity
     * Dùng cho create new tournament
     */
    public GiaiDau toEntity(TournamentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        GiaiDau entity = new GiaiDau();
        
        // Basic info
        entity.setTenGiai(dto.getTenGiai());
        entity.setDiaDiem(dto.getDiaDiem());
        entity.setTinhThanh(dto.getTinhThanh());
        entity.setQuocGia(dto.getQuocGia());
        
        // Dates (DTO → Entity: ngayBatDau→ngayBd, ngayKetThuc→ngayKt)
        entity.setNgayBd(dto.getNgayBatDau());
        entity.setNgayKt(dto.getNgayKetThuc());
        entity.setNgayMoDangKi(dto.getNgayMoDangKi());
        entity.setNgayDongDangKi(dto.getNgayDongDangKi());
        
        // Status & Category
        entity.setTrangThai(dto.getTrangThai());
        entity.setCapDo(dto.getCapDo());
        entity.setTheLoai(dto.getTheLoai());
        entity.setNoiBat(dto.getNoiBat());
        
        // Media
        entity.setHinhAnh(dto.getHinhAnh());
        entity.setLogo(dto.getLogo());
        
        // Registration
        entity.setSoLuongToiDa(dto.getSoLuongToiDa());
        entity.setPhiThamGia(dto.getPhiThamGia());
        
        return entity;
    }
    
    /**
     * Convert TournamentDetailDTO → GiaiDau Entity
     * Dùng cho create/update với full info
     */
    public GiaiDau toEntityFromDetailDTO(TournamentDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        
        GiaiDau entity = new GiaiDau();
        
        // ID (nếu update)
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        
        // Basic info
        entity.setTenGiai(dto.getTenGiai());
        entity.setDiaDiem(dto.getDiaDiem());
        entity.setTinhThanh(dto.getTinhThanh());
        entity.setQuocGia(dto.getQuocGia());
        
        // Dates (DTO → Entity)
        entity.setNgayBd(dto.getNgayBatDau());
        entity.setNgayKt(dto.getNgayKetThuc());
        entity.setNgayMoDangKi(dto.getNgayMoDangKi());
        entity.setNgayDongDangKi(dto.getNgayDongDangKi());
        
        // Status & Category
        entity.setTrangThai(dto.getTrangThai());
        entity.setCapDo(dto.getCapDo());
        entity.setTheLoai(dto.getTheLoai());
        entity.setNoiBat(dto.getNoiBat());
        
        // Full description
        entity.setMoTa(dto.getMoTa());
        entity.setQuyDinh(dto.getQuyDinh());
        entity.setGiaiThuong(dto.getGiaiThuong());
        entity.setSanThiDau(dto.getSanThiDau());
        
        // Contact info
        entity.setDienThoai(dto.getDienThoai());
        entity.setEmail(dto.getEmail());
        entity.setWebsite(dto.getWebsite());
        
        // Media
        entity.setHinhAnh(dto.getHinhAnh());
        entity.setLogo(dto.getLogo());
        
        // Registration
        entity.setSoLuongToiDa(dto.getSoLuongToiDa());
        entity.setPhiThamGia(dto.getPhiThamGia());
        
        return entity;
    }
    
    /**
     * Update existing Entity from DTO (partial update)
     * Chỉ update các field không null trong DTO
     */
    public void updateEntityFromDTO(GiaiDau entity, TournamentDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        // Update basic info
        if (dto.getTenGiai() != null) entity.setTenGiai(dto.getTenGiai());
        if (dto.getDiaDiem() != null) entity.setDiaDiem(dto.getDiaDiem());
        if (dto.getTinhThanh() != null) entity.setTinhThanh(dto.getTinhThanh());
        if (dto.getQuocGia() != null) entity.setQuocGia(dto.getQuocGia());
        
        // Update dates (DTO → Entity: ngayBatDau→ngayBd, ngayKetThuc→ngayKt)
        if (dto.getNgayBatDau() != null) entity.setNgayBd(dto.getNgayBatDau());
        if (dto.getNgayKetThuc() != null) entity.setNgayKt(dto.getNgayKetThuc());
        if (dto.getNgayMoDangKi() != null) entity.setNgayMoDangKi(dto.getNgayMoDangKi());
        if (dto.getNgayDongDangKi() != null) entity.setNgayDongDangKi(dto.getNgayDongDangKi());
        
        // Update status
        if (dto.getTrangThai() != null) entity.setTrangThai(dto.getTrangThai());
        if (dto.getCapDo() != null) entity.setCapDo(dto.getCapDo());
        if (dto.getTheLoai() != null) entity.setTheLoai(dto.getTheLoai());
        if (dto.getNoiBat() != null) entity.setNoiBat(dto.getNoiBat());
        
        // Update media
        if (dto.getHinhAnh() != null) entity.setHinhAnh(dto.getHinhAnh());
        if (dto.getLogo() != null) entity.setLogo(dto.getLogo());
        
        // Update registration
        if (dto.getSoLuongToiDa() != null) entity.setSoLuongToiDa(dto.getSoLuongToiDa());
        if (dto.getPhiThamGia() != null) entity.setPhiThamGia(dto.getPhiThamGia());
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Truncate text to specified length with ellipsis
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
