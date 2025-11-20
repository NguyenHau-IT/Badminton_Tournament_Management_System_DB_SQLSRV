package com.example.btms.web.dto;

import java.time.LocalDate;

/**
 * DTO cho Calendar Event - Sự kiện lịch thi đấu
 * 
 * USE CASE:
 * - FullCalendar.js plugin (trang lịch thi đấu)
 * - Google Calendar integration
 * - iCal export
 * - Timeline view
 * 
 * Format theo chuẩn của FullCalendar.js:
 * {
 *   "id": "1",
 *   "title": "Giải cầu lông Hà Nội mở rộng",
 *   "start": "2025-12-01",
 *   "end": "2025-12-05",
 *   "url": "/tournament/1",
 *   "color": "#007bff",
 *   "description": "...",
 *   "location": "Hà Nội"
 * }
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform
 */
public class TournamentCalendarEventDTO {
    
    // ===== FullCalendar.js Required Fields =====
    private String id;               // Event ID (phải là String cho FullCalendar)
    private String title;            // Tên giải (hiển thị trên calendar)
    private String start;            // Ngày bắt đầu (YYYY-MM-DD format)
    private String end;              // Ngày kết thúc (YYYY-MM-DD format)
    
    // ===== Optional Fields =====
    private String url;              // Link đến trang chi tiết (/tournament/{id})
    private String color;            // Màu event (theo trạng thái)
    private String backgroundColor;  // Background color
    private String borderColor;      // Border color
    private String textColor;        // Text color
    
    // ===== Extended Props (custom data) =====
    private String description;      // Mô tả ngắn
    private String location;         // Địa điểm (tỉnh/thành)
    private String status;           // Trạng thái (registration, ongoing, completed)
    private String level;            // Cấp độ (professional, amateur, youth)
    private Boolean featured;        // Giải nổi bật
    private String imageUrl;         // URL ảnh thumbnail
    
    // ===== Display Options =====
    private Boolean allDay;          // Event cả ngày (default: true cho giải đấu)
    private Boolean editable;        // Cho phép kéo thả trên calendar (default: false)
    
    // ===== CONSTRUCTORS =====
    
    public TournamentCalendarEventDTO() {
        this.allDay = true;          // Giải đấu thường kéo dài cả ngày
        this.editable = false;       // Không cho phép edit trên calendar
    }
    
    /**
     * Constructor từ Entity data
     */
    public TournamentCalendarEventDTO(Integer tournamentId, String tenGiai, 
                                     LocalDate ngayBatDau, LocalDate ngayKetThuc,
                                     String trangThai, String tinhThanh) {
        this();
        this.id = String.valueOf(tournamentId);
        this.title = tenGiai;
        this.start = ngayBatDau != null ? ngayBatDau.toString() : null;
        this.end = ngayKetThuc != null ? ngayKetThuc.toString() : null;
        this.url = "/tournament/" + tournamentId;
        this.status = trangThai;
        this.location = tinhThanh;
        
        // Auto-set color based on status
        this.color = getColorForStatus(trangThai);
    }
    
    // ===== GETTERS & SETTERS =====
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStart() {
        return start;
    }
    
    public void setStart(String start) {
        this.start = start;
    }
    
    public String getEnd() {
        return end;
    }
    
    public void setEnd(String end) {
        this.end = end;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
        // Sync với background color nếu chưa set
        if (this.backgroundColor == null) {
            this.backgroundColor = color;
        }
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public String getBorderColor() {
        return borderColor;
    }
    
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public Boolean getFeatured() {
        return featured;
    }
    
    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getAllDay() {
        return allDay;
    }
    
    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }
    
    public Boolean getEditable() {
        return editable;
    }
    
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Get màu sắc theo trạng thái giải đấu
     */
    private String getColorForStatus(String status) {
        if (status == null) return "#6c757d"; // Gray (default)
        
        switch (status.toLowerCase()) {
            case "registration":
                return "#28a745"; // Green - đang mở đăng ký
            case "ongoing":
                return "#007bff"; // Blue - đang diễn ra
            case "upcoming":
                return "#17a2b8"; // Light blue - sắp diễn ra
            case "completed":
                return "#6c757d"; // Gray - đã kết thúc
            case "cancelled":
                return "#dc3545"; // Red - đã hủy
            case "draft":
                return "#ffc107"; // Yellow - nháp
            default:
                return "#6c757d"; // Gray
        }
    }
    
    /**
     * Get tooltip text cho calendar event
     */
    public String getTooltipText() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (location != null) {
            sb.append("\n📍 ").append(location);
        }
        if (description != null) {
            sb.append("\n").append(description);
        }
        return sb.toString();
    }
    
    /**
     * Check xem event có phải featured không (để highlight)
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.featured);
    }
    
    @Override
    public String toString() {
        return "TournamentCalendarEventDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
