package com.example.btms.ui.screenshot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// UDP receive disabled per requirement: read images from local folder only
import com.example.btms.util.ui.ButtonFactory;

/**
 * Tab để hiển thị screenshot từ các client
 */
public class ScreenshotTab extends JPanel {

    private JPanel historyScreenshotPanel; // Panel cho ảnh lịch sử
    private JTextArea logArea;

    public ScreenshotTab() {
        setLayout(new BorderLayout(10, 10));
        // Khởi tạo UI và load ảnh lịch sử
        buildUI();
        SwingUtilities.invokeLater(() -> loadHistoryScreenshots());
    }

    private void buildUI() {
        // Header với controls
        // Tạo tabbed pane chỉ cho phần lịch sử ảnh từ thư mục
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 3: Ảnh các trận đã thi đấu từ folder screenshots
        JPanel historyTab = new JPanel(new BorderLayout());
        historyTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel cho ảnh lịch sử
        historyScreenshotPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        historyScreenshotPanel.setBorder(BorderFactory.createTitledBorder("Ảnh các trận đã thi đấu"));

        // Nút refresh và controls
        JButton btnRefreshHistory = ButtonFactory.filled("🔄 Làm mới", new Color(155, 89, 182), Color.WHITE,
                new Dimension(150, 35), new Font("SansSerif", Font.BOLD, 14));
        btnRefreshHistory.addActionListener(e -> loadHistoryScreenshots());

        JButton btnOpenFolder = ButtonFactory.outlined("📁 Mở thư mục", new Color(46, 204, 113), new Dimension(150, 35),
                new Font("SansSerif", Font.BOLD, 14));
        btnOpenFolder.addActionListener(e -> openScreenshotsFolder());

        // Thêm: Chọn tất cả / Bỏ chọn / Xóa đã chọn
        JButton btnSelectAll = ButtonFactory.outlined("✓ Chọn tất cả", new Color(33, 150, 243), new Dimension(140, 35),
                new Font("SansSerif", Font.BOLD, 12));
        btnSelectAll.addActionListener(e -> selectAllHistory(true));

        JButton btnClearSelection = ButtonFactory.outlined("✕ Bỏ chọn", new Color(158, 158, 158),
                new Dimension(120, 35),
                new Font("SansSerif", Font.BOLD, 12));
        btnClearSelection.addActionListener(e -> selectAllHistory(false));

        JButton btnDeleteSelected = ButtonFactory.filled("🗑️ Xóa đã chọn", new Color(244, 67, 54), Color.WHITE,
                new Dimension(150, 35), new Font("SansSerif", Font.BOLD, 12));
        btnDeleteSelected.addActionListener(e -> deleteSelectedHistory());

        JPanel historyControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        historyControls.setOpaque(false);
        historyControls.add(btnRefreshHistory);
        historyControls.add(btnOpenFolder);
        historyControls.add(btnSelectAll);
        historyControls.add(btnClearSelection);
        historyControls.add(btnDeleteSelected);

        historyTab.add(historyControls, BorderLayout.NORTH);

        JScrollPane historyScrollPane = new JScrollPane(historyScreenshotPanel);
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(48);
        historyScrollPane.getViewport().setScrollMode(javax.swing.JViewport.BACKINGSTORE_SCROLL_MODE);
        historyScrollPane.setPreferredSize(new Dimension(800, 500));
        historyTab.add(historyScrollPane, BorderLayout.CENTER);

        // Thêm tab lịch sử
        tabbedPane.addTab("📚 Lịch sử trận đấu", new ImageIcon(), historyTab);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setRows(8);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log hoạt động"));
        add(tabbedPane, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void cleanup() {
        // No background receiver anymore
    }

    /**
     * Load ảnh từ folder screenshots
     */
    private void loadHistoryScreenshots() {
        try {
            // Xóa panel cũ
            historyScreenshotPanel.removeAll();

            // Đường dẫn đến folder screenshots
            File screenshotsFolder = new File("screenshots");
            if (!screenshotsFolder.exists()) {
                screenshotsFolder.mkdirs();
                log("Đã tạo thư mục screenshots");
                return;
            }

            // Lấy danh sách file ảnh
            File[] imageFiles = screenshotsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg"));

            if (imageFiles == null || imageFiles.length == 0) {
                log("Không tìm thấy ảnh nào trong thư mục screenshots");
                return;
            }

            // Sắp xếp theo thời gian sửa đổi (mới nhất trước)
            java.util.Arrays.sort(imageFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            int loadedCount = 0;
            for (File imageFile : imageFiles) {
                try {
                    BufferedImage image = ImageIO.read(imageFile);
                    if (image != null) {
                        // Tạo thông tin file
                        String fileName = imageFile.getName();
                        String fileSize = String.format("%.1f KB", imageFile.length() / 1024.0);
                        String lastModified = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                                .format(new Date(imageFile.lastModified()));

                        String fileInfo = String.format("File: %s\nKích thước: %s\nCập nhật: %s",
                                fileName, fileSize, lastModified);

                        // Tạo item hiển thị
                        HistoryScreenshotItem item = new HistoryScreenshotItem(fileName, fileInfo, image, imageFile);
                        historyScreenshotPanel.add(item);
                        loadedCount++;
                    }
                } catch (java.io.IOException e) {
                    log("Không thể đọc file: " + imageFile.getName() + " - " + e.getMessage());
                }
            }

            // Cập nhật layout
            historyScreenshotPanel.revalidate();
            historyScreenshotPanel.repaint();

            log("Đã load " + loadedCount + " ảnh từ thư mục screenshots");

        } catch (RuntimeException e) {
            log("Lỗi khi load ảnh lịch sử: " + e.getMessage());
        }
    }

    // Đánh dấu chọn/bỏ chọn tất cả ảnh lịch sử
    private void selectAllHistory(boolean selected) {
        for (int i = 0; i < historyScreenshotPanel.getComponentCount(); i++) {
            java.awt.Component c = historyScreenshotPanel.getComponent(i);
            if (c instanceof HistoryScreenshotItem item) {
                item.setSelected(selected);
            }
        }
        historyScreenshotPanel.repaint();
    }

    // Xóa các ảnh đã chọn
    private void deleteSelectedHistory() {
        java.util.List<HistoryScreenshotItem> toDelete = new java.util.ArrayList<>();
        for (int i = 0; i < historyScreenshotPanel.getComponentCount(); i++) {
            java.awt.Component c = historyScreenshotPanel.getComponent(i);
            if (c instanceof HistoryScreenshotItem item) {
                if (item.isSelected())
                    toDelete.add(item);
            }
        }
        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn ảnh nào để xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa " + toDelete.size() + " ảnh đã chọn?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        int ok = 0, fail = 0;
        for (HistoryScreenshotItem item : toDelete) {
            try {
                File f = item.getImageFile();
                if (f != null && f.exists() && f.delete()) {
                    historyScreenshotPanel.remove(item);
                    ok++;
                } else {
                    fail++;
                }
            } catch (Exception ex) {
                fail++;
            }
        }
        historyScreenshotPanel.revalidate();
        historyScreenshotPanel.repaint();
        JOptionPane.showMessageDialog(this,
                "Đã xóa: " + ok + (fail > 0 ? (", thất bại: " + fail) : ""),
                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Mở thư mục screenshots
     */
    private void openScreenshotsFolder() {
        try {
            File screenshotsFolder = new File("screenshots");
            if (!screenshotsFolder.exists()) {
                screenshotsFolder.mkdirs();
            }

            // Mở thư mục bằng file explorer của hệ điều hành
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(screenshotsFolder);
                log("Đã mở thư mục screenshots");
            } else {
                log("Không thể mở thư mục screenshots (Desktop không được hỗ trợ)");
            }
        } catch (java.io.IOException | SecurityException e) {
            log("Lỗi khi mở thư mục screenshots: " + e.getMessage());
        }
    }

    // Đã bỏ tính năng admin tự chụp

    // Panel bo góc vẽ nền + viền để giao diện mềm mại hơn
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;

        public RoundedPanel() {
            this(12, new Color(250, 250, 250), new Color(220, 220, 220));
        }

        public RoundedPanel(int radius, Color bgColor, Color borderColor) {
            this.radius = Math.max(4, radius);
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Fill background rounded rect
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius));
                // Border
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, radius, radius));
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /**
     * Component để hiển thị một screenshot
     */
    // Đã bỏ ScreenshotItem vì không còn panel admin/client

    /**
     * Component để hiển thị một ảnh lịch sử từ folder screenshots
     */
    private static class HistoryScreenshotItem extends RoundedPanel {
        private final BufferedImage image;
        private final File imageFile;
        private final javax.swing.JCheckBox selectBox = new javax.swing.JCheckBox();

        public HistoryScreenshotItem(String fileName, String fileInfo, BufferedImage image, File imageFile) {
            this.image = image;
            this.imageFile = imageFile;

            setLayout(new BorderLayout(6, 6));
            setBorder(new EmptyBorder(8, 8, 8, 8));

            // Hiển thị ảnh với tỷ lệ đúng
            ImageIcon icon = new ImageIcon(this.image);

            // Tính toán kích thước mới giữ nguyên tỷ lệ
            int maxWidth = 250;
            int maxHeight = 200;
            int originalWidth = this.image.getWidth();
            int originalHeight = this.image.getHeight();

            double scaleX = (double) maxWidth / originalWidth;
            double scaleY = (double) maxHeight / originalHeight;
            double scale = Math.min(scaleX, scaleY); // Lấy tỷ lệ nhỏ hơn để giữ nguyên tỷ lệ

            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);

            // Thông tin file (khởi tạo trước để tái sử dụng)
            JTextArea infoArea = new JTextArea();
            infoArea.setText(fileInfo);
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);
            infoArea.setRows(3);
            infoArea.setFont(new Font("Monospaced", Font.PLAIN, 10));

            // Thanh trên: checkbox chọn + tiêu đề + nút mở file
            JButton btnOpen = new JButton("📂 Mở file");
            btnOpen.setPreferredSize(new Dimension(100, 25));
            btnOpen.addActionListener(e -> openImageFile());

            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            topBar.setOpaque(false);
            JLabel title = new JLabel(fileName);
            title.setFont(title.getFont().deriveFont(Font.BOLD));
            topBar.add(selectBox);
            topBar.add(title);
            topBar.add(btnOpen);

            add(topBar, BorderLayout.NORTH);
            add(imageLabel, BorderLayout.CENTER);

            // Thêm thông tin file ở dưới cùng
            add(infoArea, BorderLayout.SOUTH);
        }

        private void openImageFile() {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(imageFile);
                }
            } catch (java.io.IOException | SecurityException e) {
                System.err.println("Không thể mở file: " + e.getMessage());
            }
        }

        public boolean isSelected() {
            return selectBox.isSelected();
        }

        public void setSelected(boolean b) {
            selectBox.setSelected(b);
        }

        public File getImageFile() {
            return imageFile;
        }
    }

}
