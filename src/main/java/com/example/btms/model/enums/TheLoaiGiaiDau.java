package com.example.btms.model.enums;

/**
 * Enum cho thể loại giải đấu (Tournament Type)
 * 
 * Phân loại theo hình thức tổ chức và tham gia
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum TheLoaiGiaiDau {
    
    /**
     * OPEN - Giải mở
     * Ai cũng có thể đăng ký tham gia
     * Không giới hạn đối tượng
     */
    OPEN("open", "Giải mở", "Mở cho tất cả mọi người tham gia"),
    
    /**
     * INVITATIONAL - Giải mời
     * Chỉ những người được mời mới tham gia được
     * Thường dành cho VĐV đạt thành tích
     */
    INVITATIONAL("invitational", "Giải mời", "Chỉ dành cho những người được mời"),
    
    /**
     * LEAGUE - Giải đấu liên đoàn
     * Giải đấu theo mùa, có bảng xếp hạng
     * Thi đấu vòng tròn hoặc theo giai đoạn
     */
    LEAGUE("league", "Giải liên đoàn", "Giải đấu theo mùa với bảng xếp hạng"),
    
    /**
     * KNOCKOUT - Giải loại trực tiếp
     * Thua là loại ngay
     * Thường dùng cho giải đấu nhanh
     */
    KNOCKOUT("knockout", "Loại trực tiếp", "Thua là loại, không có cơ hội thứ hai"),
    
    /**
     * ROUND_ROBIN - Vòng tròn
     * Tất cả đấu với tất cả
     * Tính điểm để xếp hạng
     */
    ROUND_ROBIN("round_robin", "Vòng tròn", "Tất cả thi đấu với tất cả"),
    
    /**
     * MIXED - Hỗn hợp
     * Kết hợp nhiều hình thức
     * Ví dụ: Vòng bảng + Knockout
     */
    MIXED("mixed", "Hỗn hợp", "Kết hợp nhiều hình thức thi đấu");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    TheLoaiGiaiDau(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Convert từ String code sang Enum
     */
    public static TheLoaiGiaiDau fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TheLoaiGiaiDau type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tournament type code: " + code);
    }
    
    /**
     * Check xem có phải giải mở không
     */
    public boolean isOpen() {
        return this == OPEN;
    }
    
    /**
     * Check xem có yêu cầu mời không
     */
    public boolean requiresInvitation() {
        return this == INVITATIONAL;
    }
}
