package com.example.btms.model.enums;

/**
 * Enum cho trạng thái giải đấu (Tournament Status)
 * 
 * Lifecycle của giải đấu:
 * DRAFT → UPCOMING → REGISTRATION → ONGOING → COMPLETED (hoặc CANCELLED)
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum TrangThaiGiaiDau {
    
    /**
     * DRAFT - Nháp
     * Giải đấu đang được tạo, chưa công bố
     * Chỉ người tạo và admin mới thấy được
     */
    DRAFT("draft", "Nháp", "Giải đấu đang được soạn thảo"),
    
    /**
     * UPCOMING - Sắp diễn ra
     * Giải đấu đã công bố nhưng chưa mở đăng ký
     * Người dùng có thể xem thông tin nhưng chưa đăng ký được
     */
    UPCOMING("upcoming", "Sắp diễn ra", "Giải đấu sẽ diễn ra trong tương lai"),
    
    /**
     * REGISTRATION - Đang mở đăng ký
     * Giải đấu đang trong thời gian nhận đăng ký
     * Người dùng có thể đăng ký tham gia
     */
    REGISTRATION("registration", "Đang mở đăng ký", "Đang nhận đăng ký tham gia"),
    
    /**
     * ONGOING - Đang diễn ra
     * Giải đấu đang thi đấu
     * Hiển thị lịch thi đấu, kết quả trực tiếp
     */
    ONGOING("ongoing", "Đang diễn ra", "Giải đấu đang thi đấu"),
    
    /**
     * COMPLETED - Đã kết thúc
     * Giải đấu đã hoàn thành
     * Hiển thị kết quả cuối cùng, thống kê
     */
    COMPLETED("completed", "Đã kết thúc", "Giải đấu đã hoàn thành"),
    
    /**
     * CANCELLED - Đã hủy
     * Giải đấu bị hủy bỏ
     * Không còn hiệu lực
     */
    CANCELLED("cancelled", "Đã hủy", "Giải đấu đã bị hủy bỏ");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    TrangThaiGiaiDau(String code, String displayName, String description) {
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
     * Dùng để mapping từ database
     */
    public static TrangThaiGiaiDau fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TrangThaiGiaiDau status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown tournament status code: " + code);
    }
    
    /**
     * Check xem có phải trạng thái active không
     * (đang diễn ra hoặc đang mở đăng ký)
     */
    public boolean isActive() {
        return this == ONGOING || this == REGISTRATION;
    }
    
    /**
     * Check xem có thể đăng ký không
     */
    public boolean canRegister() {
        return this == REGISTRATION;
    }
    
    /**
     * Check xem đã kết thúc chưa
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }
}
