package com.example.btms.model.gallery;

import com.example.btms.model.tournament.GiaiDau;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity cho bảng TOURNAMENT_GALLERY
 * Quản lý media (images, videos, documents) của giải đấu
 * 
 * Database Table: TOURNAMENT_GALLERY
 * Foreign Key: ID_GIAI -> GIAI_DAU.ID (CASCADE DELETE)
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
@Entity
@Table(name = "TOURNAMENT_GALLERY")
public class TournamentGallery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    
    /**
     * Many-to-One relationship với GiaiDau
     * LAZY loading để tối ưu performance
     * Khi giải đấu bị xóa, tất cả gallery items sẽ bị xóa theo (CASCADE DELETE)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GIAI", nullable = false)
    private GiaiDau giaiDau;
    
    @Column(name = "LOAI", nullable = false, length = 20)
    private String loai; // image, video, document
    
    @Column(name = "URL", nullable = false, length = 500)
    private String url;
    
    @Column(name = "TIEU_DE", length = 200)
    private String tieuDe;
    
    @Column(name = "MO_TA", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;
    
    @Column(name = "THU_TU")
    private Integer thuTu;
    
    @Column(name = "THOI_GIAN_TAO", nullable = false)
    private LocalDateTime thoiGianTao;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default constructor for JPA
     */
    public TournamentGallery() {
        this.thoiGianTao = LocalDateTime.now();
        this.thuTu = 0;
    }
    
    /**
     * Constructor với giải đấu và basic info
     */
    public TournamentGallery(GiaiDau giaiDau, String loai, String url) {
        this();
        this.giaiDau = giaiDau;
        this.loai = loai;
        this.url = url;
    }
    
    /**
     * Full constructor
     */
    public TournamentGallery(GiaiDau giaiDau, String loai, String url, String tieuDe, String moTa, Integer thuTu) {
        this();
        this.giaiDau = giaiDau;
        this.loai = loai;
        this.url = url;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.thuTu = thuTu;
    }
    
    // ==================== GETTERS & SETTERS ====================
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public GiaiDau getGiaiDau() {
        return giaiDau;
    }
    
    public void setGiaiDau(GiaiDau giaiDau) {
        this.giaiDau = giaiDau;
    }
    
    public String getLoai() {
        return loai;
    }
    
    public void setLoai(String loai) {
        this.loai = loai;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getTieuDe() {
        return tieuDe;
    }
    
    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
    
    public Integer getThuTu() {
        return thuTu;
    }
    
    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }
    
    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }
    
    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if this is an image
     */
    public boolean isImage() {
        return "image".equalsIgnoreCase(this.loai);
    }
    
    /**
     * Check if this is a video
     */
    public boolean isVideo() {
        return "video".equalsIgnoreCase(this.loai);
    }
    
    /**
     * Check if this is a document
     */
    public boolean isDocument() {
        return "document".equalsIgnoreCase(this.loai);
    }
    
    /**
     * Get tournament ID (helper method to avoid lazy loading)
     */
    public Integer getGiaiDauId() {
        return this.giaiDau != null ? this.giaiDau.getId() : null;
    }
    
    /**
     * Auto-update timestamp before update
     */
    @PreUpdate
    public void preUpdate() {
        // Có thể thêm logic auto-update nếu cần
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentGallery that = (TournamentGallery) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TournamentGallery{" +
                "id=" + id +
                ", loai='" + loai + '\'' +
                ", tieuDe='" + tieuDe + '\'' +
                ", thuTu=" + thuTu +
                ", giaiDauId=" + getGiaiDauId() +
                '}';
    }
}
