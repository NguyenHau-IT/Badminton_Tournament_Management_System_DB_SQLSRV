package com.example.btms.util.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.Window;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class IconUtil {
    private IconUtil() {
    }

    /** Tải bộ icon app nhiều size từ /icons/app-XX.png để gán cho Frame/Taskbar. */
    public static List<Image> loadAppIcons() {
        String[] sizes = { "16", "32", "48", "64", "128", "256" };
        List<Image> icons = new ArrayList<>();
        for (String s : sizes) {
            URL url = IconUtil.class.getResource("/icons/app-" + s + ".png");
            if (url != null) {
                try {
                    icons.add(ImageIO.read(url));
                } catch (IOException ignore) {
                }
            }
        }
        return icons;
    }

    /**
     * Gán icon app cho Window (JFrame/JDialog) hoặc Panel (tự tìm Window chứa nó).
     */
    public static void applyTo(Component c) {
        List<Image> icons = loadAppIcons();
        if (icons.isEmpty())
            return;

        // Nếu là Frame
        if (c instanceof Frame f) {
            f.setIconImages(icons);
        }
        // Nếu là Dialog
        else if (c instanceof Dialog d) {
            d.setIconImage(icons.get(icons.size() - 1));
        }
        // Nếu là Panel hoặc component bất kỳ → tìm Window cha
        else if (c instanceof JPanel || c instanceof JComponent) {
            Window w = SwingUtilities.getWindowAncestor(c);
            if (w != null) {
                if (w instanceof Frame f)
                    f.setIconImages(icons);
                else if (w instanceof Dialog d)
                    d.setIconImage(icons.get(icons.size() - 1));
            }
        }

        // Taskbar / Dock icon
        try {
            Taskbar.getTaskbar().setIconImage(icons.get(icons.size() - 1));
        } catch (UnsupportedOperationException | SecurityException ignored) {
        }
    }

    /** Tạo avatar tròn từ /icons/avatar.png với đường kính cho trước. */
    public static ImageIcon loadRoundAvatar(int diameter) {
        try {
            URL url = IconUtil.class.getResource("/icons/avatar.png");
            if (url == null)
                return null;
            BufferedImage raw = ImageIO.read(url);
            int size = Math.min(raw.getWidth(), raw.getHeight());
            BufferedImage square = raw.getSubimage((raw.getWidth() - size) / 2, (raw.getHeight() - size) / 2, size,
                    size);
            Image scaled = square.getScaledInstance(diameter, diameter, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (IOException e) {
            return null;
        }
    }
}
