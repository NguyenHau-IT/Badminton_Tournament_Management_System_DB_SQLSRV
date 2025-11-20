package com.example.btms.model.enums;

/**
 * Enum cho loại media trong gallery (Media Type)
 * 
 * Phân loại các file media trong TOURNAMENT_GALLERY
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform Enhancement
 */
public enum LoaiMedia {
    
    /**
     * IMAGE - Hình ảnh
     * Các file ảnh: jpg, png, gif, webp
     * Hiển thị trong gallery, thumbnail
     */
    IMAGE("image", "Hình ảnh", "File ảnh (JPG, PNG, GIF, WebP)", 
          new String[]{"jpg", "jpeg", "png", "gif", "webp", "svg"}),
    
    /**
     * VIDEO - Video
     * Các file video: mp4, avi, mov
     * Hoặc link YouTube, Vimeo
     */
    VIDEO("video", "Video", "File video hoặc link YouTube/Vimeo",
          new String[]{"mp4", "avi", "mov", "wmv", "flv", "mkv"}),
    
    /**
     * DOCUMENT - Tài liệu
     * Các file tài liệu: pdf, doc, xls
     * Thể lệ, quy định, kết quả
     */
    DOCUMENT("document", "Tài liệu", "File tài liệu (PDF, DOC, XLS)",
             new String[]{"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"}),
    
    /**
     * POSTER - Poster/Banner
     * Ảnh poster, banner quảng cáo giải đấu
     * Dùng cho marketing
     */
    POSTER("poster", "Poster", "Poster/Banner giải đấu",
           new String[]{"jpg", "jpeg", "png", "webp"}),
    
    /**
     * LOGO - Logo
     * Logo của giải đấu
     * Dùng để nhận diện thương hiệu
     */
    LOGO("logo", "Logo", "Logo giải đấu",
         new String[]{"png", "svg", "jpg", "jpeg"});
    
    private final String code;
    private final String displayName;
    private final String description;
    private final String[] allowedExtensions;
    
    LoaiMedia(String code, String displayName, String description, String[] allowedExtensions) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.allowedExtensions = allowedExtensions;
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
    
    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }
    
    /**
     * Convert từ String code sang Enum
     */
    public static LoaiMedia fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LoaiMedia type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown media type code: " + code);
    }
    
    /**
     * Check xem file extension có hợp lệ không
     */
    public boolean isValidExtension(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.toLowerCase().replaceAll("^\\.", "");
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Detect media type từ file extension
     */
    public static LoaiMedia fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String ext = extension.toLowerCase().replaceAll("^\\.", "");
        for (LoaiMedia type : values()) {
            if (type.isValidExtension(ext)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Check xem có phải ảnh không
     */
    public boolean isImage() {
        return this == IMAGE || this == POSTER || this == LOGO;
    }
    
    /**
     * Check xem có phải video không
     */
    public boolean isVideo() {
        return this == VIDEO;
    }
    
    /**
     * Check xem có phải document không
     */
    public boolean isDocument() {
        return this == DOCUMENT;
    }
}
