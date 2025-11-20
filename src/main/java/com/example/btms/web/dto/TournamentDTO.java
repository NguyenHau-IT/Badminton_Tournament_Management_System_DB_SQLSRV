package com.example.btms.web.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * DTO cho danh sách giải đấu (Tournament List)
 * 
 * USE CASE: Trang danh sách giải đấu, search results
 * 
 * Chỉ chứa thông tin CƠ BẢN để hiển thị trong list:
 * - Tên giải, địa điểm, ngày tháng
 * - Trạng thái, cấp độ
 * - Ảnh thumbnail
 * - Một vài metrics (lượt xem, đánh giá)
 * 
 * KHÔNG bao gồm: mô tả chi tiết, quy định, gallery, thống kê sâu
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
public class TournamentDTO {
    
    // ===== BASIC INFO =====
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
    private String trangThai;        // draft, upcoming, registration, ongoing, completed, cancelled
    private String capDo;            // professional, amateur, youth, beginner, club
    private String theLoai;          // open, invitational, league, knockout, round_robin, mixed
    private Boolean noiBat;          // Featured tournament?
    
    // ===== MEDIA =====
    private String hinhAnh;          // Main image URL
    private String logo;             // Logo URL
    
    // ===== METRICS =====
    private Integer luotXem;         // View count
    private BigDecimal danhGiaTb;    // Average rating (0-5)
    private Integer tongDanhGia;     // Total ratings
    
    // ===== REGISTRATION INFO =====
    private Integer soLuongToiDa;    // Max participants
    private BigDecimal phiThamGia;   // Entry fee
    
    // ===== CONSTRUCTORS =====
    
    public TournamentDTO() {
    }
    
    /**
     * Constructor với các field quan trọng nhất
     */
    public TournamentDTO(Integer id, String tenGiai, String diaDiem, LocalDate ngayBatDau, 
                         LocalDate ngayKetThuc, String trangThai, String hinhAnh) {
        this.id = id;
        this.tenGiai = tenGiai;
        this.diaDiem = diaDiem;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
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
    
    public BigDecimal getPhiThamGia() {
        return phiThamGia;
    }
    
    public void setPhiThamGia(BigDecimal phiThamGia) {
        this.phiThamGia = phiThamGia;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Check xem giải đấu có đang mở đăng ký không
     */
    public boolean isRegistrationOpen() {
        return "registration".equalsIgnoreCase(this.trangThai);
    }
    
    /**
     * Check xem giải đấu có đang diễn ra không
     */
    public boolean isOngoing() {
        return "ongoing".equalsIgnoreCase(this.trangThai);
    }
    
    /**
     * Check xem có phải giải nổi bật không
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.noiBat);
    }
    
    /**
     * Get full location (Địa điểm, Tỉnh/Thành, Quốc gia)
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
        return "TournamentDTO{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", ngayBatDau=" + ngayBatDau +
                '}';
    }
}
