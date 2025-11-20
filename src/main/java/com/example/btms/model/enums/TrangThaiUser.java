package com.example.btms.model.enums;

/**
 * Enum cho trạng thái người dùng (User Status)
 * 
 * Quản lý trạng thái tài khoản người dùng
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum TrangThaiUser {
    
    /**
     * ACTIVE - Đang hoạt động
     * Tài khoản bình thường, có thể sử dụng đầy đủ tính năng
     */
    ACTIVE("active", "Đang hoạt động", "Tài khoản đang hoạt động bình thường"),
    
    /**
     * INACTIVE - Không hoạt động
     * Tài khoản tạm thời không sử dụng
     * Có thể kích hoạt lại
     */
    INACTIVE("inactive", "Không hoạt động", "Tài khoản tạm ngừng hoạt động"),
    
    /**
     * SUSPENDED - Bị khóa
     * Tài khoản bị khóa do vi phạm
     * Cần admin mở khóa
     */
    SUSPENDED("suspended", "Bị khóa", "Tài khoản bị khóa do vi phạm quy định"),
    
    /**
     * PENDING_VERIFICATION - Chờ xác thực
     * Tài khoản mới đăng ký, chưa xác thực email
     * Chức năng bị giới hạn
     */
    PENDING_VERIFICATION("pending_verification", "Chờ xác thực", "Đang chờ xác thực email"),
    
    /**
     * DELETED - Đã xóa
     * Tài khoản đã bị xóa (soft delete)
     * Không thể đăng nhập
     */
    DELETED("deleted", "Đã xóa", "Tài khoản đã bị xóa");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    TrangThaiUser(String code, String displayName, String description) {
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
    public static TrangThaiUser fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TrangThaiUser status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
    
    /**
     * Check xem tài khoản có đang active không
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Check xem có thể đăng nhập không
     */
    public boolean canLogin() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }
    
    /**
     * Check xem có bị khóa không
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }
    
    /**
     * Check xem đã bị xóa chưa
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
    
    /**
     * Check xem cần xác thực email không
     */
    public boolean needsVerification() {
        return this == PENDING_VERIFICATION;
    }
}
