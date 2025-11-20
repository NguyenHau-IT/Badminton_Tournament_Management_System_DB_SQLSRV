package com.example.btms.web.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO cho Tournament Statistics - Thống kê giải đấu
 * 
 * USE CASE:
 * - Dashboard/Admin panel
 * - Tournament analytics page
 * - Reports và charts
 * - Performance metrics
 * 
 * Chứa các số liệu thống kê:
 * - View count, ratings, engagement
 * - Registration stats
 * - Timeline trends
 * - Comparison data
 * 
 * Dùng để phân tích hiệu suất, đưa ra quyết định
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
public class TournamentStatsDTO {
    
    // ===== BASIC INFO =====
    private Integer tournamentId;
    private String tenGiai;
    private String trangThai;
    
    // ===== ENGAGEMENT METRICS (Chỉ số tương tác) =====
    private Integer luotXem;                    // Tổng lượt xem
    private Integer luotXemHomNay;              // Lượt xem hôm nay
    private Integer luotXemTuanNay;             // Lượt xem tuần này
    private Integer luotXemThangNay;            // Lượt xem tháng này
    
    private BigDecimal danhGiaTb;               // Điểm đánh giá trung bình (0-5)
    private Integer tongDanhGia;                // Tổng số đánh giá
    
    // Rating breakdown (phân bổ rating)
    private Integer rating5Stars;               // Số người cho 5 sao
    private Integer rating4Stars;               // Số người cho 4 sao
    private Integer rating3Stars;               // Số người cho 3 sao
    private Integer rating2Stars;               // Số người cho 2 sao
    private Integer rating1Star;                // Số người cho 1 sao
    
    // ===== REGISTRATION METRICS (Chỉ số đăng ký) =====
    private Integer soLuongToiDa;               // Giới hạn người tham gia
    private Integer soLuongDaDangKy;            // Số người đã đăng ký
    private Integer soLuongDangChoDuyet;        // Số đăng ký đang chờ duyệt
    private Integer soLuongDaDuyet;             // Số đăng ký đã được duyệt
    private Integer soLuongDaTuChoi;            // Số đăng ký bị từ chối
    
    private BigDecimal tyLeLapDay;              // % lấp đầy (đã đăng ký / max)
    
    // ===== TIME METRICS (Chỉ số thời gian) =====
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private LocalDate ngayMoDangKi;
    private LocalDate ngayDongDangKi;
    
    private Integer soNgayConLai;               // Số ngày còn lại đến khi diễn ra
    private Integer soNgayDangKyConLai;         // Số ngày còn lại để đăng ký
    
    // ===== FINANCIAL METRICS (Chỉ số tài chính) =====
    private BigDecimal phiThamGia;              // Phí tham gia
    private BigDecimal tongDoanhThu;            // Tổng doanh thu (phí * số người đã duyệt)
    private BigDecimal doanhThuDuKien;          // Doanh thu dự kiến (phí * số người đăng ký)
    
    // ===== COMPARISON DATA (Dữ liệu so sánh) =====
    private Integer rankingByViews;             // Xếp hạng theo lượt xem (trong tháng)
    private Integer rankingByRating;            // Xếp hạng theo đánh giá
    private Double percentileViews;             // Percentile lượt xem (top x%)
    
    // ===== TREND DATA (Dữ liệu xu hướng) =====
    private Map<String, Integer> viewsByDate;   // Lượt xem theo ngày (cho chart)
    private Map<String, Integer> registrationsByDate; // Đăng ký theo ngày (cho chart)
    
    // ===== GEOGRAPHIC DATA (Dữ liệu địa lý) =====
    private Map<String, Integer> participantsByProvince; // Số người tham gia theo tỉnh
    
    // ===== CONSTRUCTORS =====
    
    public TournamentStatsDTO() {
    }
    
    public TournamentStatsDTO(Integer tournamentId, String tenGiai, String trangThai) {
        this.tournamentId = tournamentId;
        this.tenGiai = tenGiai;
        this.trangThai = trangThai;
    }
    
    // ===== GETTERS & SETTERS =====
    
    public Integer getTournamentId() {
        return tournamentId;
    }
    
    public void setTournamentId(Integer tournamentId) {
        this.tournamentId = tournamentId;
    }
    
    public String getTenGiai() {
        return tenGiai;
    }
    
    public void setTenGiai(String tenGiai) {
        this.tenGiai = tenGiai;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public Integer getLuotXem() {
        return luotXem;
    }
    
    public void setLuotXem(Integer luotXem) {
        this.luotXem = luotXem;
    }
    
    public Integer getLuotXemHomNay() {
        return luotXemHomNay;
    }
    
    public void setLuotXemHomNay(Integer luotXemHomNay) {
        this.luotXemHomNay = luotXemHomNay;
    }
    
    public Integer getLuotXemTuanNay() {
        return luotXemTuanNay;
    }
    
    public void setLuotXemTuanNay(Integer luotXemTuanNay) {
        this.luotXemTuanNay = luotXemTuanNay;
    }
    
    public Integer getLuotXemThangNay() {
        return luotXemThangNay;
    }
    
    public void setLuotXemThangNay(Integer luotXemThangNay) {
        this.luotXemThangNay = luotXemThangNay;
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
    
    public Integer getRating5Stars() {
        return rating5Stars;
    }
    
    public void setRating5Stars(Integer rating5Stars) {
        this.rating5Stars = rating5Stars;
    }
    
    public Integer getRating4Stars() {
        return rating4Stars;
    }
    
    public void setRating4Stars(Integer rating4Stars) {
        this.rating4Stars = rating4Stars;
    }
    
    public Integer getRating3Stars() {
        return rating3Stars;
    }
    
    public void setRating3Stars(Integer rating3Stars) {
        this.rating3Stars = rating3Stars;
    }
    
    public Integer getRating2Stars() {
        return rating2Stars;
    }
    
    public void setRating2Stars(Integer rating2Stars) {
        this.rating2Stars = rating2Stars;
    }
    
    public Integer getRating1Star() {
        return rating1Star;
    }
    
    public void setRating1Star(Integer rating1Star) {
        this.rating1Star = rating1Star;
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
    
    public Integer getSoLuongDangChoDuyet() {
        return soLuongDangChoDuyet;
    }
    
    public void setSoLuongDangChoDuyet(Integer soLuongDangChoDuyet) {
        this.soLuongDangChoDuyet = soLuongDangChoDuyet;
    }
    
    public Integer getSoLuongDaDuyet() {
        return soLuongDaDuyet;
    }
    
    public void setSoLuongDaDuyet(Integer soLuongDaDuyet) {
        this.soLuongDaDuyet = soLuongDaDuyet;
    }
    
    public Integer getSoLuongDaTuChoi() {
        return soLuongDaTuChoi;
    }
    
    public void setSoLuongDaTuChoi(Integer soLuongDaTuChoi) {
        this.soLuongDaTuChoi = soLuongDaTuChoi;
    }
    
    public BigDecimal getTyLeLapDay() {
        return tyLeLapDay;
    }
    
    public void setTyLeLapDay(BigDecimal tyLeLapDay) {
        this.tyLeLapDay = tyLeLapDay;
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
    
    public Integer getSoNgayConLai() {
        return soNgayConLai;
    }
    
    public void setSoNgayConLai(Integer soNgayConLai) {
        this.soNgayConLai = soNgayConLai;
    }
    
    public Integer getSoNgayDangKyConLai() {
        return soNgayDangKyConLai;
    }
    
    public void setSoNgayDangKyConLai(Integer soNgayDangKyConLai) {
        this.soNgayDangKyConLai = soNgayDangKyConLai;
    }
    
    public BigDecimal getPhiThamGia() {
        return phiThamGia;
    }
    
    public void setPhiThamGia(BigDecimal phiThamGia) {
        this.phiThamGia = phiThamGia;
    }
    
    public BigDecimal getTongDoanhThu() {
        return tongDoanhThu;
    }
    
    public void setTongDoanhThu(BigDecimal tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }
    
    public BigDecimal getDoanhThuDuKien() {
        return doanhThuDuKien;
    }
    
    public void setDoanhThuDuKien(BigDecimal doanhThuDuKien) {
        this.doanhThuDuKien = doanhThuDuKien;
    }
    
    public Integer getRankingByViews() {
        return rankingByViews;
    }
    
    public void setRankingByViews(Integer rankingByViews) {
        this.rankingByViews = rankingByViews;
    }
    
    public Integer getRankingByRating() {
        return rankingByRating;
    }
    
    public void setRankingByRating(Integer rankingByRating) {
        this.rankingByRating = rankingByRating;
    }
    
    public Double getPercentileViews() {
        return percentileViews;
    }
    
    public void setPercentileViews(Double percentileViews) {
        this.percentileViews = percentileViews;
    }
    
    public Map<String, Integer> getViewsByDate() {
        return viewsByDate;
    }
    
    public void setViewsByDate(Map<String, Integer> viewsByDate) {
        this.viewsByDate = viewsByDate;
    }
    
    public Map<String, Integer> getRegistrationsByDate() {
        return registrationsByDate;
    }
    
    public void setRegistrationsByDate(Map<String, Integer> registrationsByDate) {
        this.registrationsByDate = registrationsByDate;
    }
    
    public Map<String, Integer> getParticipantsByProvince() {
        return participantsByProvince;
    }
    
    public void setParticipantsByProvince(Map<String, Integer> participantsByProvince) {
        this.participantsByProvince = participantsByProvince;
    }
    
    // ===== CALCULATED HELPER METHODS =====
    
    /**
     * Tính % lấp đầy
     */
    public void calculateTyLeLapDay() {
        if (soLuongToiDa != null && soLuongToiDa > 0 && soLuongDaDangKy != null) {
            this.tyLeLapDay = BigDecimal.valueOf(soLuongDaDangKy)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(soLuongToiDa), 2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Tính tổng doanh thu thực tế
     */
    public void calculateTongDoanhThu() {
        if (phiThamGia != null && soLuongDaDuyet != null) {
            this.tongDoanhThu = phiThamGia.multiply(BigDecimal.valueOf(soLuongDaDuyet));
        }
    }
    
    /**
     * Tính doanh thu dự kiến
     */
    public void calculateDoanhThuDuKien() {
        if (phiThamGia != null && soLuongDaDangKy != null) {
            this.doanhThuDuKien = phiThamGia.multiply(BigDecimal.valueOf(soLuongDaDangKy));
        }
    }
    
    /**
     * Check xem có đạt mục tiêu lượt xem không (VD: 1000 views)
     */
    public boolean reachedViewTarget(int target) {
        return luotXem != null && luotXem >= target;
    }
    
    /**
     * Check xem có đạt mục tiêu đăng ký không
     */
    public boolean reachedRegistrationTarget(int targetPercentage) {
        if (tyLeLapDay == null) {
            calculateTyLeLapDay();
        }
        return tyLeLapDay != null && tyLeLapDay.intValue() >= targetPercentage;
    }
    
    /**
     * Get engagement score (0-100) dựa trên views, ratings, registrations
     */
    public int getEngagementScore() {
        int score = 0;
        
        // Views contribution (40 points max)
        if (luotXem != null) {
            score += Math.min(luotXem / 25, 40); // 1000 views = 40 points
        }
        
        // Rating contribution (30 points max)
        if (danhGiaTb != null) {
            score += (danhGiaTb.multiply(BigDecimal.valueOf(6))).intValue(); // 5.0 rating = 30 points
        }
        
        // Registration contribution (30 points max)
        if (tyLeLapDay != null) {
            score += Math.min(tyLeLapDay.multiply(BigDecimal.valueOf(0.3)).intValue(), 30); // 100% = 30 points
        }
        
        return Math.min(score, 100);
    }
    
    @Override
    public String toString() {
        return "TournamentStatsDTO{" +
                "tournamentId=" + tournamentId +
                ", tenGiai='" + tenGiai + '\'' +
                ", luotXem=" + luotXem +
                ", soLuongDaDangKy=" + soLuongDaDangKy +
                ", danhGiaTb=" + danhGiaTb +
                '}';
    }
}
