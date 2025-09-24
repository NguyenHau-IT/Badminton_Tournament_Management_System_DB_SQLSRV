package com.example.badmintoneventtechnology.ui.theme;

import java.awt.*;

import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

/**
 * Khởi tạo Look & Feel + style bo góc toàn cục cho toàn bộ ứng dụng.
 * Tách riêng để dễ chỉnh sửa / mở rộng (dark mode, scaling...).
 */
public final class UITheme {

    private static boolean inited = false;

    private UITheme() {
    }

    public static void init() {
        if (inited)
            return;
        inited = true;
        try {
            // Bật window decorations của FlatLaf để có tiêu đề đồng bộ và bo góc (nếu OS hỗ
            // trợ)
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");

            // Cho phép load file properties tùy biến trong resources/themes
            FlatLaf.registerCustomDefaultsSource("themes");
            FlatLightLaf.setup();

            // Tự tính kích thước bo góc theo DPI (cảm giác mềm mại trên màn 4K)
            float scale = getScaleFactor(); // ~1.0 - 2.0
            int arcLarge = Math.round(20 * scale);
            int arcMedium = Math.round(16 * scale);
            int arcSmall = Math.round(12 * scale);

            // Thiết lập arc toàn cục
            UIManager.put("Component.arc", arcLarge);
            UIManager.put("Button.arc", arcLarge);
            UIManager.put("TextComponent.arc", arcMedium);
            UIManager.put("CheckBox.arc", arcSmall);
            UIManager.put("ProgressBar.arc", arcMedium);
            UIManager.put("ScrollBar.thumbArc", arcMedium);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.tabArc", arcMedium);
            UIManager.put("PopupMenu.borderCornerRadius", arcSmall);
            UIManager.put("Table.selectionArc", arcSmall);

            // Giảm viền focus đậm
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);

            // Làm nổi bật màu accent (có thể thay đổi mã màu để phù hợp branding)
            UIManager.put("Component.focusColor", new Color(0x2D89EF));
            UIManager.put("Button.focusedBorderColor", new Color(0x2D89EF));

            // Làm mềm bảng / list
            UIManager.put("Table.showHorizontalLines", Boolean.FALSE);
            UIManager.put("Table.showVerticalLines", Boolean.FALSE);
            UIManager.put("List.cellFocusColor", new Color(0, 0, 0, 0));

            // Bo góc menu bar/title
            UIManager.put("TitlePane.showIcon", Boolean.TRUE);
            UIManager.put("TitlePane.unifiedBackground", Boolean.TRUE);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static float getScaleFactor() {
        try {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            int w = dm.getWidth();
            // Heuristic đơn giản: màn cực rộng => scale lớn
            if (w >= 3800)
                return 1.5f;
            if (w >= 2560)
                return 1.25f;
            return 1.0f;
        } catch (Exception ignore) {
            return 1.0f;
        }
    }
}
