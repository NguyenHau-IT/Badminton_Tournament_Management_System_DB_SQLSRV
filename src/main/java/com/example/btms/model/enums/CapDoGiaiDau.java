package com.example.btms.model.enums;

/**
 * Enum cho cấp độ giải đấu (Tournament Level)
 * 
 * Phân loại giải đấu theo trình độ thi đấu
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum CapDoGiaiDau {
    
    /**
     * PROFESSIONAL - Chuyên nghiệp
     * Giải đấu dành cho VĐV chuyên nghiệp
     * Có giải thưởng cao, yêu cầu trình độ cao
     */
    PROFESSIONAL("professional", "Chuyên nghiệp", "Dành cho vận động viên chuyên nghiệp"),
    
    /**
     * AMATEUR - Nghiệp dư
     * Giải đấu dành cho người chơi nghiệp dư
     * Phổ biến nhất, mở cho nhiều đối tượng
     */
    AMATEUR("amateur", "Nghiệp dư", "Dành cho người chơi nghiệp dư"),
    
    /**
     * YOUTH - Thiếu niên
     * Giải đấu dành cho lứa tuổi thiếu niên
     * Thường có giới hạn độ tuổi
     */
    YOUTH("youth", "Thiếu niên", "Dành cho lứa tuổi thiếu niên"),
    
    /**
     * BEGINNER - Người mới
     * Giải đấu dành cho người mới chơi
     * Trình độ cơ bản
     */
    BEGINNER("beginner", "Người mới", "Dành cho người mới bắt đầu"),
    
    /**
     * CLUB - Câu lạc bộ
     * Giải đấu nội bộ câu lạc bộ
     * Thân thiện, giao lưu
     */
    CLUB("club", "Câu lạc bộ", "Giải đấu nội bộ câu lạc bộ");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    CapDoGiaiDau(String code, String displayName, String description) {
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
    public static CapDoGiaiDau fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CapDoGiaiDau level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown tournament level code: " + code);
    }
    
    /**
     * Check xem có phải cấp độ cao không
     */
    public boolean isProfessional() {
        return this == PROFESSIONAL;
    }
    
    /**
     * Check xem có phải giải nghiệp dư không
     */
    public boolean isAmateur() {
        return this == AMATEUR || this == CLUB || this == BEGINNER;
    }
}
