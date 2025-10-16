package com.example.btms.util.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Swing UI utilities */
public final class UiUtil {
    private UiUtil() {
    }

    /**
     * Tháo component khỏi parent nếu đang được gắn ở nơi khác (tránh
     * IllegalArgumentException)
     */
    public static void detachFromParent(Component c) {
        if (c != null && c.getParent() instanceof Container) {
            Container parent = (Container) c.getParent();
            parent.remove(c);
            parent.revalidate();
            parent.repaint();
        }
    }

    /** Load một ảnh từ classpath (resources). Trả về null nếu không tìm thấy. */
    public static Image loadImage(String classpath) {
        URL url = UiUtil.class.getResource(classpath);
        return (url != null) ? new ImageIcon(url).getImage() : null;
    }

    /**
     * Load danh sách ảnh (đa kích thước) từ classpath. Bỏ qua path bị null/không
     * tồn tại.
     */
    public static List<Image> loadImages(String... classpaths) {
        List<Image> out = new ArrayList<>();
        if (classpaths == null)
            return out;
        for (String p : classpaths) {
            if (p == null || p.isBlank())
                continue;
            Image img = loadImage(p);
            if (img != null)
                out.add(img);
        }
        return out;
    }

    /**
     * Gán icon cho JFrame (đa kích thước). Đồng thời set Taskbar icon trên macOS
     * nếu có.
     */
    public static void applyWindowIcons(Window win, List<Image> icons) {
        if (win instanceof Frame f && icons != null && !icons.isEmpty()) {
            f.setIconImages(icons);
            // macOS: set icon cho Dock (không bắt buộc)
            try {
                Taskbar.getTaskbar().setIconImage(icons.get(Math.min(1, icons.size() - 1)));
            } catch (UnsupportedOperationException | SecurityException ignore) {
            }
        }
    }

    /**
     * Bọc 1 JComponent vào JFrame và hiển thị (có icon taskbar).
     * 
     * @param content   component (ví dụ LoginTab)
     * @param title     tiêu đề
     * @param iconPaths classpath icon, ví dụ: "/icons/app-16.png",
     *                  "/icons/app-32.png", ...
     * @return JFrame đã hiển thị (để caller gắn thêm listener nếu cần)
     */
    public static JFrame showInFrame(JComponent content, String title, String... iconPaths) {
        detachFromParent(content);

        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(content);
        f.pack();
        f.setLocationRelativeTo(null);

        List<Image> icons = loadImages(iconPaths);
        applyWindowIcons(f, icons);

        f.setVisible(true);
        return f;
    }

    /**
     * Tạo JFrame nhưng CHƯA hiển thị — cho phép caller cấu hình thêm rồi mới
     * setVisible(true).
     */
    public static JFrame buildFrame(JComponent content, String title, String... iconPaths) {
        detachFromParent(content);

        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(content);
        f.pack();
        f.setLocationRelativeTo(null);

        List<Image> icons = loadImages(iconPaths);
        applyWindowIcons(f, icons);

        return f;
    }

    /** Tiện ích: bật/ẩn toàn bộ inputs của 1 container (nếu cần dùng nhanh). */
    public static void setInputsEnabled(Container root, boolean enabled) {
        for (Component c : root.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container cc)
                setInputsEnabled(cc, enabled);
        }
    }
}