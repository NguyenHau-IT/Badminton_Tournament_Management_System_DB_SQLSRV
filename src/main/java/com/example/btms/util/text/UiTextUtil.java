package com.example.btms.util.text;

/**
 * Tiện ích xử lý chuỗi cho UI:
 * - Giữ nguyên khoảng trắng khi render HTML (space -> &nbsp;).
 * - Tách tên thành 2 dòng tại dấu cách đầu tiên (giữ nguyên các khoảng trắng
 * còn lại).
 * - Xử lý tên đội đánh đôi thông minh.
 */
public final class UiTextUtil {
    private UiTextUtil() {
    }

    /** Escape HTML & giữ nguyên khoảng trắng: ' ' -> &nbsp; */
    public static String htmlPreserveSpaces(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case ' ' -> sb.append("&nbsp;");
                case '-' -> sb.append("&#8209;"); // Non-breaking hyphen
                default -> sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Biến chuỗi thành HTML 2 dòng, tách tại dấu cách đầu tiên (từ trái sang).
     * - Không trim bên phải để KHÔNG làm mất các dấu cách tiếp theo.
     * - Nếu uppercase=true sẽ chuyển sang chữ hoa trước khi xử lý.
     */
    public static String toTwoLinesAtFirstSpaceHtml(String s, boolean uppercase) {
        if (s == null || s.isBlank())
            return "";
        String t = uppercase ? s.toUpperCase() : s;
        int i = t.indexOf(' ');
        if (i >= 0 && i < t.length() - 1) {
            String left = htmlPreserveSpaces(t.substring(0, i));
            String right = htmlPreserveSpaces(t.substring(i + 1)); // KHÔNG trim
            return "<html>" + left + "<br>" + right + "</html>";
        }
        return "<html>" + htmlPreserveSpaces(t) + "</html>";
    }

    /** Xoá đoạn trong dấu ngoặc tròn (kể cả dấu ngoặc) */
    public static String removeParentheses(String s) {
        if (s == null)
            return "";
        return s.replaceAll("\\s*\\([^)]*\\)", "");
        // \\s* để xoá luôn khoảng trắng thừa trước ngoặc
    }

    /**
     * Xử lý tên đội đánh đôi để hiển thị tối ưu
     * - Xóa dấu ngoặc và nội dung bên trong
     * - Xóa các ký tự đặc biệt không cần thiết
     * - Chuẩn hóa khoảng trắng
     */
    public static String processDoublesName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "";
        }

        // Xóa dấu ngoặc và nội dung bên trong
        String cleaned = rawName.replaceAll("\\s*\\([^)]*\\)", "");

        // Xóa các ký tự đặc biệt không cần thiết
        cleaned = cleaned.replaceAll("[\\[\\]{}]", "");

        // Xóa các ký tự đặc biệt khác
        cleaned = cleaned.replaceAll("[^\\w\\s\\-]", "");

        // Chuẩn hóa khoảng trắng
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Tạo hiển thị 2 dòng thông minh cho tên đội
     * - Tự động tìm điểm chia tối ưu
     * - Hỗ trợ căn lề trái hoặc giữa
     * - Xử lý tên dài và ngắn
     */
    public static String createTwoLineDisplay(String name, boolean uppercase, boolean centerAlign) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String processed = uppercase ? name.toUpperCase() : name;

        // Tìm vị trí tốt nhất để chia tên thành 2 dòng
        int splitIndex = findOptimalSplitPoint(processed);

        if (splitIndex > 0 && splitIndex < processed.length() - 1) {
            String firstLine = processed.substring(0, splitIndex).trim();
            String secondLine = processed.substring(splitIndex).trim();

            // Đảm bảo cả 2 dòng đều có nội dung
            if (!firstLine.isEmpty() && !secondLine.isEmpty()) {
                String align = centerAlign ? "center" : "left";
                return "<html><div style='text-align: " + align + "; line-height: 1.2;'>" +
                        htmlPreserveSpaces(firstLine) + "<br>" +
                        htmlPreserveSpaces(secondLine) + "</div></html>";
            }
        }

        // Nếu không thể chia hoặc tên quá ngắn, hiển thị 1 dòng
        String align = centerAlign ? "center" : "left";
        return "<html><div style='text-align: " + align + ";'>" +
                htmlPreserveSpaces(processed) + "</div></html>";
    }

    /**
     * Tạo hiển thị 2 dòng thông minh cho tên đội (căn lề trái)
     */
    public static String createTwoLineDisplay(String name, boolean uppercase) {
        return createTwoLineDisplay(name, uppercase, false);
    }

    /**
     * Tìm điểm chia tối ưu cho tên đội
     * - Ưu tiên dấu cách
     * - Tiếp theo là dấu gạch ngang
     * - Cuối cùng là điểm chia cân bằng
     */
    public static int findOptimalSplitPoint(String name) {
        if (name.length() <= 15) {
            // Tên ngắn, chia ở giữa
            return name.length() / 2;
        }

        // Tìm dấu cách đầu tiên
        int firstSpace = name.indexOf(' ');
        if (firstSpace > 0 && firstSpace < name.length() - 1) {
            return firstSpace;
        }

        // Tìm dấu gạch ngang
        int firstDash = name.indexOf('-');
        if (firstDash > 0 && firstDash < name.length() - 1) {
            return firstDash;
        }

        // Tìm điểm chia cân bằng (khoảng 60% chiều dài)
        return (int) (name.length() * 0.6);
    }

    /**
     * Tạo tên hiển thị ngắn gọn cho đội
     * - Giữ lại tên chính của mỗi VĐV
     * - Loại bỏ thông tin phụ
     */
    public static String createCompactTeamName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String cleaned = processDoublesName(fullName);

        // Nếu có dấu gạch ngang, chỉ lấy phần đầu
        int dashIndex = cleaned.indexOf('-');
        if (dashIndex > 0) {
            cleaned = cleaned.substring(0, dashIndex).trim();
        }

        return cleaned;
    }

    /**
     * Kiểm tra xem tên có phải là đội đánh đôi không
     */
    public static boolean isDoublesTeamName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        // Kiểm tra các dấu hiệu của đội đánh đôi
        return name.contains(" - ") ||
                name.contains("-") ||
                name.contains(" & ") ||
                name.contains(" và ") ||
                name.contains("(") ||
                name.contains(")");
    }
}
