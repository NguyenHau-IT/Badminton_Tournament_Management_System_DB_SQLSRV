package com.example.btms.model.enums;

/**
 * Enum cho vai trò người dùng (User Role)
 * 
 * Phân quyền trong hệ thống Web Platform
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum VaiTro {
    
    /**
     * ADMIN - Quản trị viên
     * Quyền cao nhất trong hệ thống
     * Có thể làm mọi thứ: quản lý users, tournaments, settings
     */
    ADMIN("ADMIN", "Quản trị viên", "Quyền quản trị toàn bộ hệ thống"),
    
    /**
     * ORGANIZER - Người tổ chức
     * Có thể tạo và quản lý giải đấu
     * Quản lý đăng ký, kết quả thi đấu
     */
    ORGANIZER("ORGANIZER", "Người tổ chức", "Tạo và quản lý giải đấu"),
    
    /**
     * PLAYER - Vận động viên
     * Có thể đăng ký tham gia giải đấu
     * Xem kết quả, lịch thi đấu cá nhân
     */
    PLAYER("PLAYER", "Vận động viên", "Tham gia thi đấu"),
    
    /**
     * REFEREE - Trọng tài
     * Có thể nhập kết quả trận đấu
     * Quản lý scoreboard
     */
    REFEREE("REFEREE", "Trọng tài", "Điều hành và ghi nhận kết quả thi đấu"),
    
    /**
     * CLIENT - Người dùng thông thường
     * Chỉ có thể xem thông tin công khai
     * Không có quyền đặc biệt
     */
    CLIENT("CLIENT", "Người dùng", "Xem thông tin giải đấu");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    VaiTro(String code, String displayName, String description) {
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
    public static VaiTro fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (VaiTro role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
    
    /**
     * Check xem có phải admin không
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Check xem có quyền tạo giải đấu không
     */
    public boolean canCreateTournament() {
        return this == ADMIN || this == ORGANIZER;
    }
    
    /**
     * Check xem có quyền quản lý giải đấu không
     */
    public boolean canManageTournament() {
        return this == ADMIN || this == ORGANIZER;
    }
    
    /**
     * Check xem có thể tham gia thi đấu không
     */
    public boolean canParticipate() {
        return this == PLAYER || this == ORGANIZER;
    }
    
    /**
     * Check xem có quyền nhập kết quả không
     */
    public boolean canEnterResults() {
        return this == ADMIN || this == ORGANIZER || this == REFEREE;
    }
}
