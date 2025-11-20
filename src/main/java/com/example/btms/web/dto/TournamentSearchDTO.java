package com.example.btms.web.dto;

/**
 * DTO cho Search Autocomplete - Kết quả tìm kiếm gợi ý
 * 
 * USE CASE:
 * - Autocomplete dropdown trong search box
 * - Hiển thị top 5-10 kết quả phù hợp
 * - Preview nhanh thông tin giải đấu
 * 
 * Đây là DTO CỰC KỲ NHẸ - chỉ gồm thông tin tối thiểu cho preview:
 * - ID để link đến detail page
 * - Tên giải để highlight keyword
 * - Thumbnail nhỏ
 * - Tỉnh thành và ngày để context
 * - Status badge để phân biệt loại giải
 * 
 * @author BTMS Team
 * @version 1.0 - Web Platform Autocomplete
 */
public class TournamentSearchDTO {
    
    private Integer id;
    private String tenGiai;
    private String hinhAnh;
    private String tinhThanh;
    private String ngayBatDau;    // Format: "15/03/2025" (String để dễ hiển thị)
    private String trangThai;     // ongoing, upcoming, registration, completed
    
    // ===== CONSTRUCTORS =====
    
    public TournamentSearchDTO() {
    }
    
    public TournamentSearchDTO(Integer id, String tenGiai, String hinhAnh, 
                              String tinhThanh, String ngayBatDau, String trangThai) {
        this.id = id;
        this.tenGiai = tenGiai;
        this.hinhAnh = hinhAnh;
        this.tinhThanh = tinhThanh;
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
    
    public String getNgayBatDau() {
        return ngayBatDau;
    }
    
    public void setNgayBatDau(String ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    // ===== BUSINESS METHODS =====
    
    /**
     * Get display URL cho hình ảnh
     * Nếu không có hình thì dùng placeholder
     */
    public String getDisplayImage() {
        if (hinhAnh == null || hinhAnh.isEmpty()) {
            return "/images/tournament-placeholder.jpg";
        }
        return hinhAnh;
    }
    
    /**
     * Get formatted status text (Vietnamese)
     */
    public String getStatusText() {
        if (trangThai == null) return "Khác";
        
        return switch (trangThai.toLowerCase()) {
            case "ongoing" -> "ĐANG DIỄN RA";
            case "upcoming" -> "SẮP DIỄN RA";
            case "registration" -> "MỞ ĐĂNG KÝ";
            case "completed" -> "ĐÃ KẾT THÚC";
            default -> "KHÁC";
        };
    }
    
    @Override
    public String toString() {
        return "TournamentSearchDTO{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", tinhThanh='" + tinhThanh + '\'' +
                ", ngayBatDau='" + ngayBatDau + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
