package com.example.btms.web.dto;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO cho Tournament Card - Thẻ giải đấu nhỏ gọn
 * 
 * USE CASE: 
 * - Homepage carousel (giải nổi bật)
 * - Sidebar "Recommended tournaments"
 * - Mobile app cards
 * - Email marketing
 * 
 * Đây là DTO NHẸ NHẤT - chỉ gồm thông tin tối thiểu:
 * - Tên, ảnh
 * - Ngày tháng
 * - 1-2 metrics (rating, views)
 * - Status badge
 * 
 * Tối ưu cho performance khi load nhiều cards cùng lúc
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
public class TournamentCardDTO {
    
    private Integer id;
    private String tenGiai;
    private String hinhAnh;          // Thumbnail image
    private String tinhThanh;        // Chỉ hiển thị tỉnh/thành, không cần địa chỉ đầy đủ
    
    // Dates - chỉ cần ngày bắt đầu cho card
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    
    // Status & badges
    private String trangThai;        // Hiển thị badge: "Đang đăng ký", "Đang diễn ra", v.v.
    private Boolean noiBat;          // Featured badge
    
    // Metrics - chỉ 2 số quan trọng nhất
    private BigDecimal danhGiaTb;    // Rating stars
    private Integer luotXem;         // View count (để show popularity)
    
    // Quick info
    private String capDo;            // Để hiển thị badge: "Chuyên nghiệp", "Nghiệp dư"
    private BigDecimal phiThamGia;   // Hiển thị giá (nếu free thì show "Miễn phí")
    
    // ===== CONSTRUCTORS =====
    
    public TournamentCardDTO() {
    }
    
    /**
     * Constructor cho các field cần thiết nhất
     */
    public TournamentCardDTO(Integer id, String tenGiai, String hinhAnh, 
                             LocalDate ngayBatDau, String trangThai) {
        this.id = id;
        this.tenGiai = tenGiai;
        this.hinhAnh = hinhAnh;
        this.ngayBatDau = ngayBatDau;
        this.trangThai = trangThai;
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
    
    public String getHinhAnh() {
        return hinhAnh;
    }
    
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    
    public String getTinhThanh() {
        return tinhThanh;
    }
    
    public void setTinhThanh(String tinhThanh) {
        this.tinhThanh = tinhThanh;
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
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public Boolean getNoiBat() {
        return noiBat;
    }
    
    public void setNoiBat(Boolean noiBat) {
        this.noiBat = noiBat;
    }
    
    public BigDecimal getDanhGiaTb() {
        return danhGiaTb;
    }
    
    public void setDanhGiaTb(BigDecimal danhGiaTb) {
        this.danhGiaTb = danhGiaTb;
    }
    
    public Integer getLuotXem() {
        return luotXem;
    }
    
    public void setLuotXem(Integer luotXem) {
        this.luotXem = luotXem;
    }
    
    public String getCapDo() {
        return capDo;
    }
    
    public void setCapDo(String capDo) {
        this.capDo = capDo;
    }
    
    public BigDecimal getPhiThamGia() {
        return phiThamGia;
    }
    
    public void setPhiThamGia(BigDecimal phiThamGia) {
        this.phiThamGia = phiThamGia;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Check xem có phải giải miễn phí không
     */
    public boolean isFree() {
        return phiThamGia == null || phiThamGia.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Get text hiển thị cho phí tham gia
     */
    public String getPhiThamGiaText() {
        if (isFree()) {
            return "Miễn phí";
        }
        return String.format("%,.0f VNĐ", phiThamGia);
    }
    
    /**
     * Get badge text cho trạng thái
     */
    public String getTrangThaiBadge() {
        if (trangThai == null) return "";
        
        switch (trangThai.toLowerCase()) {
            case "registration":
                return "Đang mở đăng ký";
            case "ongoing":
                return "Đang diễn ra";
            case "upcoming":
                return "Sắp diễn ra";
            case "completed":
                return "Đã kết thúc";
            default:
                return trangThai;
        }
    }
    
    /**
     * Get CSS class cho status badge
     */
    public String getTrangThaiBadgeClass() {
        if (trangThai == null) return "badge-secondary";
        
        switch (trangThai.toLowerCase()) {
            case "registration":
                return "badge-success";   // Green
            case "ongoing":
                return "badge-primary";   // Blue
            case "upcoming":
                return "badge-info";      // Light blue
            case "completed":
                return "badge-secondary"; // Gray
            case "cancelled":
                return "badge-danger";    // Red
            default:
                return "badge-secondary";
        }
    }
    
    /**
     * Check xem có phải giải nổi bật không
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.noiBat);
    }
    
    /**
     * Get số sao đánh giá (làm tròn)
     */
    public int getStarRating() {
        if (danhGiaTb == null) return 0;
        return danhGiaTb.setScale(0, RoundingMode.HALF_UP).intValue();
    }
    
    @Override
    public String toString() {
        return "TournamentCardDTO{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
