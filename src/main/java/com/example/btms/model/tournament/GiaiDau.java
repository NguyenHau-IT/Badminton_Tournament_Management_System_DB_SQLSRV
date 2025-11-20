package com.example.btms.model.tournament;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class cho bảng GIAI_DAU
 * Enhanced với Web Platform fields
 * Database: SQL Server
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Integration
 * @since 2025-11-19
 */
@Entity
@Table(name = "GIAI_DAU")
public class GiaiDau {
    
    // ===== ORIGINAL FIELDS (Desktop App) =====
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    
    @Column(name = "TEN_GIAI", nullable = false, length = 1000)
    private String tenGiai;
    
    @Column(name = "NGAY_BD", nullable = false)
    private LocalDate ngayBd;
    
    @Column(name = "NGAY_KT", nullable = false)
    private LocalDate ngayKt;
    
    @Column(name = "NGAY_TAO", nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(name = "NGAY_CAP_NHAT", nullable = false)
    private LocalDateTime ngayCapNhat;
    
    @Column(name = "ID_USER", nullable = false)
    private Integer idUser;
    
    // ===== NEW FIELDS FOR WEB PLATFORM (Phase 1) =====
    
    @Column(name = "MO_TA", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;
    
    @Column(name = "DIA_DIEM", length = 500)
    private String diaDiem;
    
    @Column(name = "TINH_THANH", length = 100)
    private String tinhThanh;
    
    @Column(name = "QUOC_GIA", length = 50)
    private String quocGia = "VN";
    
    @Column(name = "TRANG_THAI", length = 20)
    private String trangThai = "upcoming"; // upcoming/registration/ongoing/completed/cancelled
    
    @Column(name = "NOI_BAT")
    private Boolean noiBat = false;
    
    @Column(name = "HINH_ANH", length = 500)
    private String hinhAnh;
    
    @Column(name = "LOGO", length = 500)
    private String logo;
    
    @Column(name = "NGAY_MO_DANG_KI")
    private LocalDate ngayMoDangKi;
    
    @Column(name = "NGAY_DONG_DANG_KI")
    private LocalDate ngayDongDangKi;
    
    @Column(name = "SO_LUONG_TOI_DA")
    private Integer soLuongToiDa;
    
    @Column(name = "SO_LUONG_DA_DANG_KI")
    private Integer soLuongDaDangKy = 0;
    
    @Column(name = "PHI_THAM_GIA", precision = 10, scale = 2)
    private BigDecimal phiThamGia;
    
    @Column(name = "GIAI_THUONG", columnDefinition = "NVARCHAR(MAX)")
    private String giaiThuong;
    
    @Column(name = "DIEN_THOAI", length = 20)
    private String dienThoai;
    
    @Column(name = "EMAIL", length = 100)
    private String email;
    
    @Column(name = "WEBSITE", length = 200)
    private String website;
    
    @Column(name = "CAP_DO", length = 50)
    private String capDo; // professional/amateur/youth
    
    @Column(name = "THE_LOAI", length = 50)
    private String theLoai; // open/invitational/league
    
    @Column(name = "SAN_THI_DAU", columnDefinition = "NVARCHAR(MAX)")
    private String sanThiDau;
    
    @Column(name = "QUY_DINH", columnDefinition = "NVARCHAR(MAX)")
    private String quyDinh;
    
    @Column(name = "LUOT_XEM")
    private Integer luotXem = 0;
    
    @Column(name = "DANH_GIA_TB", precision = 3, scale = 2)
    private BigDecimal danhGiaTb;
    
    @Column(name = "TONG_DANH_GIA")
    private Integer tongDanhGia = 0;

    // ===== CONSTRUCTORS =====
    
    /**
     * Default constructor
     */
    public GiaiDau() {
    }

    /**
     * Constructor for Desktop App (backward compatibility)
     */
    public GiaiDau(String tenGiai, LocalDate ngayBd, LocalDate ngayKt, Integer idUser) {
        this.tenGiai = tenGiai;
        this.ngayBd = ngayBd;
        this.ngayKt = ngayKt;
        this.idUser = idUser;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
        // Web platform defaults
        this.quocGia = "VN";
        this.trangThai = "upcoming";
        this.noiBat = false;
        this.luotXem = 0;
        this.tongDanhGia = 0;
    }
    
    /**
     * Full constructor for Web Platform
     */
    public GiaiDau(String tenGiai, LocalDate ngayBd, LocalDate ngayKt, Integer idUser,
                   String moTa, String diaDiem, String tinhThanh) {
        this(tenGiai, ngayBd, ngayKt, idUser);
        this.moTa = moTa;
        this.diaDiem = diaDiem;
        this.tinhThanh = tinhThanh;
    }

    // ===== GETTERS AND SETTERS - ORIGINAL FIELDS =====
    
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

    public LocalDate getNgayBd() {
        return ngayBd;
    }

    public void setNgayBd(LocalDate ngayBd) {
        this.ngayBd = ngayBd;
    }

    public LocalDate getNgayKt() {
        return ngayKt;
    }

    public void setNgayKt(LocalDate ngayKt) {
        this.ngayKt = ngayKt;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }
    
    // ===== GETTERS AND SETTERS - NEW WEB PLATFORM FIELDS =====
    
    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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

    public String getGiaiThuong() {
        return giaiThuong;
    }

    public void setGiaiThuong(String giaiThuong) {
        this.giaiThuong = giaiThuong;
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

    public String getSanThiDau() {
        return sanThiDau;
    }

    public void setSanThiDau(String sanThiDau) {
        this.sanThiDau = sanThiDau;
    }

    public String getQuyDinh() {
        return quyDinh;
    }

    public void setQuyDinh(String quyDinh) {
        this.quyDinh = quyDinh;
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

    // ===== UTILITY METHODS =====
    
    /**
     * Update timestamp to current time
     */
    @PreUpdate
    public void updatestamp() {
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Check if tournament is currently active/ongoing
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return ngayBd != null && ngayKt != null &&
                (now.isEqual(ngayBd) || now.isAfter(ngayBd)) && 
                (now.isEqual(ngayKt) || now.isBefore(ngayKt));
    }

    /**
     * Check if tournament is upcoming
     */
    public boolean isUpcoming() {
        LocalDate now = LocalDate.now();
        return ngayBd != null && now.isBefore(ngayBd);
    }

    /**
     * Check if tournament is finished
     */
    public boolean isFinished() {
        LocalDate now = LocalDate.now();
        return ngayKt != null && now.isAfter(ngayKt);
    }
    
    /**
     * Check if registration is open
     */
    public boolean isRegistrationOpen() {
        LocalDate now = LocalDate.now();
        return ngayMoDangKi != null && ngayDongDangKi != null &&
                (now.isEqual(ngayMoDangKi) || now.isAfter(ngayMoDangKi)) &&
                (now.isEqual(ngayDongDangKi) || now.isBefore(ngayDongDangKi));
    }
    
    /**
     * Increment view count
     */
    public void incrementViewCount() {
        if (this.luotXem == null) {
            this.luotXem = 0;
        }
        this.luotXem++;
    }
    
    /**
     * Update average rating
     */
    public void updateAverageRating(BigDecimal newRating) {
        if (this.tongDanhGia == null) {
            this.tongDanhGia = 0;
        }
        if (this.danhGiaTb == null) {
            this.danhGiaTb = BigDecimal.ZERO;
        }
        
        // Calculate new average: (old_avg * old_count + new_rating) / (old_count + 1)
        BigDecimal totalScore = this.danhGiaTb.multiply(BigDecimal.valueOf(this.tongDanhGia));
        totalScore = totalScore.add(newRating);
        this.tongDanhGia++;
        this.danhGiaTb = totalScore.divide(BigDecimal.valueOf(this.tongDanhGia), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Check if tournament is featured
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.noiBat);
    }

    // ===== OBJECT METHODS =====
    
    @Override
    public String toString() {
        return "GiaiDau{" +
                "id=" + id +
                ", tenGiai='" + tenGiai + '\'' +
                ", ngayBd=" + ngayBd +
                ", ngayKt=" + ngayKt +
                ", trangThai='" + trangThai + '\'' +
                ", noiBat=" + noiBat +
                ", tinhThanh='" + tinhThanh + '\'' +
                ", luotXem=" + luotXem +
                ", ngayTao=" + ngayTao +
                ", ngayCapNhat=" + ngayCapNhat +
                ", idUser=" + idUser +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiaiDau giaiDau = (GiaiDau) o;
        return id != null && id.equals(giaiDau.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
