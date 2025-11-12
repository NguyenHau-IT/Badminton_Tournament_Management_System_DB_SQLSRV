package com.example.btms.ui.welcome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Màn hình chào mừng cho ADMIN (Java 21, HiDPI-friendly)
 * - Nền gradient (có dark mode)
 * - Card "glass", bóng đổ mềm (clamp alpha)
 * - Avatar tròn load từ /icons/btms.png (downscale BICUBIC multi-step + cache
 * theo DPI)
 * - 3 nút hành động (hover)
 *
 * API:
 * - setTournamentName(String)
 * - setOnOpenManagement(ActionListener)
 * - setOnCreateTournament(ActionListener)
 * - setOnOpenGuide(ActionListener)
 * - setDarkMode(boolean), isDarkMode()
 */
public class AdminWelcomePanel extends JPanel {

    private final JLabel title = new JLabel("Chào mừng, ADMINISTRATOR", SwingConstants.CENTER);
    private final JLabel subtitle = new JLabel("Chưa chọn giải", SwingConstants.CENTER);
    private final JLabel hint = new JLabel("Dùng menu Quản lý để vào Giải đấu, Nội dung, VĐV…", SwingConstants.CENTER);

    private final JButton btnOpen = new JButton("Vào quản lý giải");
    private final JButton btnCreate = new JButton("Tạo giải mới");
    private final JButton btnGuide = new JButton("Hướng dẫn nhanh");

    private boolean darkMode = false; // mặc định sáng

    public AdminWelcomePanel() {
        setOpaque(true);
        setLayout(new GridBagLayout()); // canh giữa

        // ---- Typography
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 13f));
        applyTextColors();

        // ---- Card glassmorphism
        JPanel card = new GlassCard();
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(36, 36, 32, 36)); // tăng padding trong để card to, thoáng
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(900, 480)); // card to hơn

        // Header (avatar + text)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // 🔄 thay avatar -> btms.png và tăng size để hợp card
        JLabel iconLabel = loadCircularAvatar("/icons/btms.png", 160);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel textBlock = new JPanel(new GridLayout(3, 1, 0, 6));
        textBlock.setOpaque(false);
        textBlock.add(title);
        textBlock.add(subtitle);
        textBlock.add(hint);

        header.add(iconLabel, BorderLayout.NORTH);
        header.add(textBlock, BorderLayout.CENTER);

        // Buttons row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        stylePrimary(btnOpen);
        styleSecondary(btnCreate);
        styleTertiary(btnGuide);
        actions.add(btnOpen);
        actions.add(btnCreate);
        actions.add(btnGuide);

        card.add(header, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        // Holder có bóng đổ mềm AN TOÀN (clamp alpha)
        JPanel shadowHolder = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 28;
                int pad = 8;
                int w = Math.max(0, getWidth() - pad * 2);
                int h = Math.max(0, getHeight() - pad * 2);
                if (w <= 0 || h <= 0) {
                    g2.dispose();
                    return;
                }

                Shape shadow = new RoundRectangle2D.Float(pad, pad, w, h, arc, arc);

                for (int i = 12; i >= 1; i--) {
                    float alphaF = 0.035f * (i / 12f); // tối đa ~0.035
                    int a = Math.max(0, Math.min(255, Math.round(255f * alphaF))); // clamp
                    g2.setColor(new Color(0, 0, 0, a));
                    g2.translate(0, 1); // bóng lệch xuống dưới nhẹ
                    g2.fill(shadow);
                }
                g2.dispose();
            }
        };
        shadowHolder.setOpaque(false);
        shadowHolder.setBorder(new EmptyBorder(8, 8, 16, 8));
        shadowHolder.add(card, BorderLayout.CENTER);

        // Đặt vào center
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(16, 16, 16, 16);
        add(shadowHolder, gbc);
    }

    // ---------------- Public API ----------------

    /** Cập nhật tên giải hiển thị. */
    public void setTournamentName(String name) {
        String text = (name == null || name.isBlank()) ? "Chưa chọn giải" : ("Giải đấu: " + name);
        subtitle.setText(text);
    }

    /** Gắn action cho nút “Vào quản lý giải”. */
    public void setOnOpenManagement(ActionListener al) {
        replaceListeners(btnOpen, al);
    }

    /** Gắn action cho nút “Tạo giải mới”. */
    public void setOnCreateTournament(ActionListener al) {
        replaceListeners(btnCreate, al);
    }

    /** Gắn action cho nút “Hướng dẫn nhanh”. */
    public void setOnOpenGuide(ActionListener al) {
        replaceListeners(btnGuide, al);
    }

    /** Bật/tắt dark mode và tô lại. */
    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        applyTextColors();
        repaint();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    // ---------------- Internal styles ----------------

    private void applyTextColors() {
        if (darkMode) {
            title.setForeground(new Color(240, 240, 245));
            subtitle.setForeground(new Color(225, 225, 230));
            hint.setForeground(new Color(210, 210, 220, 200));
        } else {
            title.setForeground(new Color(20, 20, 20, 235));
            subtitle.setForeground(new Color(60, 60, 60, 230));
            hint.setForeground(new Color(40, 40, 40, 180));
        }
    }

    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setBorder(new RoundedBorder(14));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(new Color(255, 255, 255));
        b.setBackground(new Color(16, 132, 255));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(10, 18, 10, 18));
        installHover(b, new Color(16, 132, 255), new Color(10, 110, 230));
    }

    private void styleSecondary(JButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setBorder(new RoundedBorder(14));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(darkMode ? new Color(240, 240, 245) : new Color(30, 30, 30));
        b.setBackground(new Color(255, 255, 255, 210));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(10, 18, 10, 18));
        installHover(b, new Color(255, 255, 255, 210), new Color(255, 255, 255, 235));
    }

    private void styleTertiary(JButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 14f));
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(darkMode ? new Color(230, 230, 240, 220) : new Color(30, 30, 30, 200));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setForeground(darkMode ? new Color(250, 250, 255) : new Color(20, 20, 20));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setForeground(darkMode ? new Color(230, 230, 240, 220) : new Color(30, 30, 30, 200));
            }
        });
    }

    private void installHover(JButton b, Color normalBg, Color hoverBg) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBg);
                b.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normalBg);
                b.repaint();
            }
        });
    }

    private void replaceListeners(AbstractButton b, ActionListener al) {
        for (ActionListener l : b.getActionListeners())
            b.removeActionListener(l);
        if (al != null)
            b.addActionListener(al);
    }

    // ---------------- Painting ----------------

    /** Nền gradient toàn màn hình (sáng/tối). */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (darkMode) {
            GradientPaint gp = new GradientPaint(0, 0, new Color(20, 30, 55),
                    getWidth(), getHeight(), new Color(45, 25, 60));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            RadialGradientPaint halo = new RadialGradientPaint(
                    new Point(Math.max(1, getWidth() / 3), Math.max(1, getHeight() / 4)),
                    Math.max(1, Math.max(getWidth(), getHeight()) / 2f),
                    new float[] { 0f, 1f },
                    new Color[] { new Color(90, 70, 120, 90), new Color(0, 0, 0, 0) });
            g2.setPaint(halo);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            GradientPaint gp = new GradientPaint(0, 0, new Color(180, 224, 255),
                    getWidth(), getHeight(), new Color(210, 188, 255));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            RadialGradientPaint halo = new RadialGradientPaint(
                    new Point(Math.max(1, getWidth() / 3), Math.max(1, getHeight() / 4)),
                    Math.max(1, Math.max(getWidth(), getHeight()) / 2f),
                    new float[] { 0f, 1f },
                    new Color[] { new Color(255, 255, 255, 100), new Color(255, 255, 255, 0) });
            g2.setPaint(halo);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }

    /** Card bo tròn trong suốt kiểu glass. */
    private static class GlassCard extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 28;
            Shape rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);

            // lớp mờ + viền
            g2.setColor(new Color(255, 255, 255, 175));
            g2.fill(rr);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(rr);

            // highlight nhẹ phía trên
            GradientPaint gloss = new GradientPaint(0, 0, new Color(255, 255, 255, 120),
                    0, Math.max(1, getHeight() / 2), new Color(255, 255, 255, 0));
            g2.setPaint(gloss);
            g2.fill(new RoundRectangle2D.Float(1, 1, Math.max(0, getWidth() - 2), Math.max(0, getHeight() / 2f),
                    arc - 2, arc - 2));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // ---------------- Avatar helpers ----------------

    /** Tạo JLabel icon avatar tròn từ resource; có fallback nếu không tìm thấy. */
    private JLabel loadCircularAvatar(String resourcePath, int logicalSize) {
        Image img = null;
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null)
                img = new ImageIcon(url).getImage();
        } catch (Exception ignored) {
        }

        if (img == null) {
            img = createFallbackAvatar(logicalSize * 2, darkMode ? new Color(90, 110, 160) : new Color(120, 150, 200),
                    "A");
        }
        Icon icon = new RoundedRectImageIcon(img, 700, 200, 50); // dùng class HiDPI + multi-step
        return new JLabel(icon);
    }

    /** Vẽ avatar tròn fallback với một chữ cái. */
    private static Image createFallbackAvatar(int size, Color bg, String letter) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape circle = new Ellipse2D.Float(0, 0, size, size);
        g2.setColor(bg);
        g2.fill(circle);
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(circle);

        g2.setFont(new Font("SansSerif", Font.BOLD, Math.round(size * 0.45f)));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(letter);
        int th = fm.getAscent();
        g2.drawString(letter, (size - tw) / 2, (size + th) / 2 - 6);

        g2.dispose();
        return bi;
    }

    /**
     * High-quality circular avatar with crisp downscaling & HiDPI-friendly caching.
     * - Tính đến device scale factor (Java 21): scaleX từ GraphicsConfiguration
     * - Multi-step downscale + BICUBIC để tránh mờ khi giảm kích thước mạnh
     * - Cache buffer theo kích thước thiết bị để hạn chế render lại nặng
     */
    // Thay toàn bộ class CircleImageIcon bằng class dưới đây
    private static class RoundedRectImageIcon extends ImageIcon {
        private final int logicalW; // chiều rộng mong muốn (logic)
        private final int logicalH; // chiều cao mong muốn (logic)
        private final int radius; // bo góc
        private final Image source; // ảnh gốc
        private volatile BufferedImage cached; // cache ảnh đã scale theo device DPI (cover + mask)

        RoundedRectImageIcon(Image original, int logicalW, int logicalH, int radius) {
            this.source = original;
            this.logicalW = Math.max(1, logicalW);
            this.logicalH = Math.max(1, logicalH);
            this.radius = Math.max(0, radius);
        }

        @Override
        public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double scale = 1.0;
            GraphicsConfiguration gc = c.getGraphicsConfiguration();
            if (gc != null) {
                try {
                    scale = gc.getDefaultTransform().getScaleX();
                } catch (Exception ignored) {
                }
                if (scale <= 0)
                    scale = 1.0;
            }

            int deviceW = Math.max(1, (int) Math.round(logicalW * scale));
            int deviceH = Math.max(1, (int) Math.round(logicalH * scale));

            if (cached == null || cached.getWidth() != deviceW || cached.getHeight() != deviceH) {
                cached = highQualityDownscaleToRounded(source, deviceW, deviceH, (int) Math.round(radius * scale));
            }

            // Vẽ theo kích thước logic; Java 21 map DPI tự động
            g2.drawImage(cached, x, y, logicalW, logicalH, null);

            // Viền trắng nhẹ
            Shape rr = new RoundRectangle2D.Float(x, y, logicalW, logicalH, radius, radius);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(255, 255, 255, 200));
            g2.draw(rr);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return logicalW;
        }

        @Override
        public int getIconHeight() {
            return logicalH;
        }

        private static BufferedImage highQualityDownscaleToRounded(Image src, int targetW, int targetH, int radiusPx) {
            BufferedImage argb = toARGB(src);
            BufferedImage covered = scaleImageToCover(argb, targetW, targetH); // không méo
            BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape mask = new RoundRectangle2D.Float(0, 0, targetW, targetH, radiusPx, radiusPx);
            g2.setClip(mask);
            g2.drawImage(covered, 0, 0, targetW, targetH, null);
            g2.setClip(null);
            g2.dispose();
            return out;
        }

        private static BufferedImage toARGB(Image img) {
            if (img instanceof BufferedImage bi && bi.getType() == BufferedImage.TYPE_INT_ARGB)
                return bi;
            ImageIcon ii = new ImageIcon(img); // ép load đầy đủ
            BufferedImage out = new BufferedImage(ii.getIconWidth(), ii.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.drawImage(ii.getImage(), 0, 0, null);
            g2.dispose();
            return out;
        }

        /**
         * Scale ảnh theo "cover": không méo, đủ phủ target, rồi crop giữa. Dùng
         * multi-step để mịn.
         */
        private static BufferedImage scaleImageToCover(BufferedImage src, int targetW, int targetH) {
            int sw = src.getWidth();
            int sh = src.getHeight();
            if (sw <= 0 || sh <= 0)
                return src;

            double scale = Math.max((double) targetW / sw, (double) targetH / sh);
            int newW = Math.max(1, (int) Math.round(sw * scale));
            int newH = Math.max(1, (int) Math.round(sh * scale));

            BufferedImage scaled = multiStepScale(src, newW, newH);

            // Crop giữa để vừa khít target
            int x = Math.max(0, (newW - targetW) / 2);
            int y = Math.max(0, (newH - targetH) / 2);
            BufferedImage cropped = scaled.getSubimage(x, y, Math.min(targetW, scaled.getWidth() - x),
                    Math.min(targetH, scaled.getHeight() - y));

            // đảm bảo đúng kích thước target (phòng trường hợp biên)
            if (cropped.getWidth() != targetW || cropped.getHeight() != targetH) {
                BufferedImage fixed = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = fixed.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(cropped, 0, 0, targetW, targetH, null);
                g2.dispose();
                return fixed;
            }
            return cropped;
        }

        /**
         * Multi-step downscale (1/2 dần) + BICUBIC để cực mịn. Up-scale bước cuối dùng
         * BICUBIC.
         */
        private static BufferedImage multiStepScale(BufferedImage src, int targetW, int targetH) {
            int w = src.getWidth();
            int h = src.getHeight();
            BufferedImage cur = src;

            // chỉ downscale nhiều bước khi thu nhỏ; khi phóng to thì nhảy thẳng
            while (w / 2 >= targetW && h / 2 >= targetH) {
                w /= 2;
                h /= 2;
                BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(cur, 0, 0, w, h, null);
                g2.dispose();
                if (cur != src)
                    cur.flush();
                cur = tmp;
            }

            if (w != targetW || h != targetH) {
                BufferedImage tmp = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(cur, 0, 0, targetW, targetH, null);
                g2.dispose();
                if (cur != src)
                    cur.flush();
                cur = tmp;
            }
            return cur;
        }
    }

    /** Viền bo tròn cho button. */
    private static class RoundedBorder implements javax.swing.border.Border {
        private final int arc;

        RoundedBorder(int arc) {
            this.arc = arc;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 14, 8, 14);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape rr = new RoundRectangle2D.Float(x, y, w - 1, h - 1, arc, arc);
            g2.setColor(new Color(255, 255, 255, 140));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(rr);
            g2.dispose();
        }
    }

}
