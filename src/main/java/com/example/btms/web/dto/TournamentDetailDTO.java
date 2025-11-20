package com.example.btms.web.dto;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho trang chi tiết giải đấu (Tournament Detail Page)
 * 
 * USE CASE: Trang /tournament/{id} - hiển thị TOÀN BỘ thông tin giải đấu
 * 
 * Bao gồm:
 * - Tất cả thông tin từ TournamentDTO
 * - Mô tả đầy đủ
 * - Thông tin liên hệ (email, phone, website)
 * - Quy định thi đấu
 * - Giải thưởng
 * - Gallery (danh sách ảnh/video)
 * 
 * Đây là DTO "nặng" nhất - chỉ dùng khi cần hiển thị chi tiết
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
public class TournamentDetailDTO {
    
    // ===== BASIC INFO (từ TournamentDTO) =====
    private Integer id;
    private String tenGiai;
    private String diaDiem;
    private String tinhThanh;
    private String quocGia;
    
    // ===== DATES =====
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private LocalDate ngayMoDangKi;
    private LocalDate ngayDongDangKi;
    
    // ===== STATUS & CATEGORY =====
    private String trangThai;
    private String capDo;
    private String theLoai;
    private Boolean noiBat;
    
    // ===== FULL DESCRIPTION (THÊM MỚI) =====
    private String moTa;             // Mô tả đầy đủ (có thể dài)
    private String quyDinh;          // Quy định thi đấu
    private String giaiThuong;       // Thông tin giải thưởng
    private String sanThiDau;        // Thông tin sân thi đấu
    
    // ===== CONTACT INFO (THÊM MỚI) =====
    private String dienThoai;
    private String email;
    private String website;
    
    // ===== MEDIA =====
    private String hinhAnh;
    private String logo;
    
    // ===== GALLERY (THÊM MỚI) =====
    private List<GalleryItemDTO> gallery;  // Danh sách ảnh/video
    
    // ===== METRICS =====
    private Integer luotXem;
    private BigDecimal danhGiaTb;
    private Integer tongDanhGia;
    
    // ===== REGISTRATION INFO =====
    private Integer soLuongToiDa;
    private Integer soLuongDaDangKy;  // THÊM MỚI: số người đã đăng ký (sẽ thêm sau)
    private BigDecimal phiThamGia;
    
    // ===== NESTED CLASS cho Gallery Items =====
    
    /**
     * Inner class cho mỗi item trong gallery
     */
    public static class GalleryItemDTO {
        private Integer id;
        private String loai;      // image, video, document, poster, logo
        private String url;
        private String tieuDe;
        private String moTa;
        private Integer thuTu;
        
        public GalleryItemDTO() {
        }
        
        public GalleryItemDTO(Integer id, String loai, String url, String tieuDe) {
            this.id = id;
            this.loai = loai;
            this.url = url;
            this.tieuDe = tieuDe;
        }
        
        // Getters & Setters
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
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
    }
    
    // ===== CONSTRUCTORS =====
    
    public TournamentDetailDTO() {
    }
    
    // ===== GETTERS & SETTERS =====
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTenGiai() {
        return tenGiai;
    }
    
    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }
    
    public String getDiaDiem() {
        return diaDiem;
    }
    
    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }
    
    public String getTinhThanh() {
        return tinhThanh;
    }
    
    public void setTinhThanh(String tinhThanh) {
        this.tinhThanh = tinhThanh;
    }
    
    public String getQuocGia() {
        return quocGia;
    }
    
    public void setQuocGia(String quocGia) {
        this.quocGia = quocGia;
    }
    
    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }
    
    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }
    
    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }
    
    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
    
    public LocalDate getNgayMoDangKi() {
        return ngayMoDangKi;
    }
    
    public void setNgayMoDangKi(LocalDate ngayMoDangKi) {
        this.ngayMoDangKi = ngayMoDangKi;
    }
    
    public LocalDate getNgayDongDangKi() {
        return ngayDongDangKi;
    }
    
    public void setNgayDongDangKi(LocalDate ngayDongDangKi) {
        this.ngayDongDangKi = ngayDongDangKi;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public String getCapDo() {
        return capDo;
    }
    
    public void setCapDo(String capDo) {
        this.capDo = capDo;
    }
    
    public String getTheLoai() {
        return theLoai;
    }
    
    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }
    
    public Boolean getNoiBat() {
        return noiBat;
    }
    
    public void setNoiBat(Boolean noiBat) {
        this.noiBat = noiBat;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
    
    public String getQuyDinh() {
        return quyDinh;
    }
    
    public void setQuyDinh(String quyDinh) {
        this.quyDinh = quyDinh;
    }
    
    public String getGiaiThuong() {
        return giaiThuong;
    }
    
    public void setGiaiThuong(String giaiThuong) {
        this.giaiThuong = giaiThuong;
    }
    
    public String getSanThiDau() {
        return sanThiDau;
    }
    
    public void setSanThiDau(String sanThiDau) {
        this.sanThiDau = sanThiDau;
    }
    
    public String getDienThoai() {
        return dienThoai;
    }
    
    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getHinhAnh() {
        return hinhAnh;
    }
    
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public List<GalleryItemDTO> getGallery() {
        return gallery;
    }
    
    public void setGallery(List<GalleryItemDTO> gallery) {
        this.gallery = gallery;
    }
    
    public Integer getLuotXem() {
        return luotXem;
    }
    
    public void setLuotXem(Integer luotXem) {
        this.luotXem = luotXem;
    }
    
    public BigDecimal getDanhGiaTb() {
        return danhGiaTb;
    }
    
    public void setDanhGiaTb(BigDecimal danhGiaTb) {
        this.danhGiaTb = danhGiaTb;
    }
    
    public Integer getTongDanhGia() {
        return tongDanhGia;
    }
    
    public void setTongDanhGia(Integer tongDanhGia) {
        this.tongDanhGia = tongDanhGia;
    }
    
    public Integer getSoLuongToiDa() {
        return soLuongToiDa;
    }
    
    public void setSoLuongToiDa(Integer soLuongToiDa) {
        this.soLuongToiDa = soLuongToiDa;
    }
    
    public Integer getSoLuongDaDangKy() {
        return soLuongDaDangKy;
    }
    
    public void setSoLuongDaDangKy(Integer soLuongDaDangKy) {
        this.soLuongDaDangKy = soLuongDaDangKy;
    }
    
    public BigDecimal getPhiThamGia() {
        return phiThamGia;
    }
    
    public void setPhiThamGia(BigDecimal phiThamGia) {
        this.phiThamGia = phiThamGia;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Check xem còn chỗ đăng ký không
     */
    public boolean hasAvailableSlots() {
        if (soLuongToiDa == null || soLuongDaDangKy == null) {
            return true;
        }
        return soLuongDaDangKy < soLuongToiDa;
    }
    
    /**
     * Get % đã đăng ký
     */
    public Integer getRegistrationPercentage() {
        if (soLuongToiDa == null || soLuongToiDa == 0 || soLuongDaDangKy == null) {
            return 0;
        }
        return (soLuongDaDangKy * 100) / soLuongToiDa;
    }
    
    /**
     * Get full location
     */
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (diaDiem != null) sb.append(diaDiem);
        if (tinhThanh != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(tinhThanh);
        }
        if (quocGia != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(quocGia);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "TournamentDetailDTO{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", gallerySize=" + (gallery != null ? gallery.size() : 0) +
                '}';
    }
}
