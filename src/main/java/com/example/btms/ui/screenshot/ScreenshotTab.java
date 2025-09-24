package com.example.btms.ui.screenshot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
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

import com.example.btms.service.scoreboard.ScreenshotReceiver;
import com.example.btms.util.ui.ButtonFactory;

/**
 * Tab để hiển thị screenshot từ các client
 */
public class ScreenshotTab extends JPanel implements ScreenshotReceiver.ScreenshotListener {

    private ScreenshotReceiver receiver;
    private JPanel screenshotPanel;
    private JPanel adminScreenshotPanel;
    private JPanel historyScreenshotPanel; // Panel cho ảnh lịch sử
    private JTextArea logArea;
    private JButton btnStartReceiver;
    private JButton btnStopReceiver;
    private JButton btnSaveAll;

    private boolean isReceiverRunning = false;

    public ScreenshotTab() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        startReceiver();

        // Tự động load ảnh lịch sử sau khi UI được tạo
        SwingUtilities.invokeLater(() -> loadHistoryScreenshots());
    }

    private void buildUI() {
        // Header với controls
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createTitledBorder("Điều khiển"));

        JPanel controls = new JPanel(new GridLayout(1, 3, 10, 0));
        btnStartReceiver = ButtonFactory.filled("▶ Khởi động", new Color(46, 204, 113), Color.WHITE,
                new Dimension(120, 30), new Font("SansSerif", Font.BOLD, 12));
        btnStopReceiver = ButtonFactory.filled("⏹ Dừng", new Color(231, 76, 60), Color.WHITE, new Dimension(120, 30),
                new Font("SansSerif", Font.BOLD, 12));
        btnSaveAll = ButtonFactory.outlined("💾 Lưu tất cả", new Color(30, 136, 229), new Dimension(120, 30),
                new Font("SansSerif", Font.BOLD, 12));

        btnStartReceiver.addActionListener(e -> startReceiver());
        btnStopReceiver.addActionListener(e -> stopReceiver());
        btnSaveAll.addActionListener(e -> saveAllScreenshots());

        controls.add(btnStartReceiver);
        controls.add(btnStopReceiver);
        controls.add(btnSaveAll);

        header.add(controls, BorderLayout.CENTER);

        // Tạo tabbed pane để chia 2 phần
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Screenshot từ Client
        JPanel clientTab = new JPanel(new BorderLayout());
        clientTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        screenshotPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        screenshotPanel.setBorder(BorderFactory.createTitledBorder("Screenshot từ Client"));

        JScrollPane clientScrollPane = new JScrollPane(screenshotPanel);
        clientScrollPane.setPreferredSize(new Dimension(800, 500));
        clientTab.add(clientScrollPane, BorderLayout.CENTER);

        // Tab 2: Screenshot Admin tự chụp
        JPanel adminTab = new JPanel(new BorderLayout());
        adminTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel cho admin tự chụp
        adminScreenshotPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        adminScreenshotPanel.setBorder(BorderFactory.createTitledBorder("Screenshot Admin tự chụp"));

        // Nút chụp ảnh cho admin
        JButton btnCaptureAdmin = ButtonFactory.filled("📸 Chụp ảnh màn hình", new Color(52, 152, 219), Color.WHITE,
                new Dimension(200, 35), new Font("SansSerif", Font.BOLD, 14));
        btnCaptureAdmin.addActionListener(e -> captureAdminScreenshot());

        JPanel adminControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminControls.setOpaque(false);
        adminControls.add(btnCaptureAdmin);

        adminTab.add(adminControls, BorderLayout.NORTH);

        JScrollPane adminScrollPane = new JScrollPane(adminScreenshotPanel);
        adminScrollPane.setPreferredSize(new Dimension(800, 500));
        adminTab.add(adminScrollPane, BorderLayout.CENTER);

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

        JPanel historyControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        historyControls.setOpaque(false);
        historyControls.add(btnRefreshHistory);
        historyControls.add(btnOpenFolder);

        historyTab.add(historyControls, BorderLayout.NORTH);

        JScrollPane historyScrollPane = new JScrollPane(historyScreenshotPanel);
        historyScrollPane.setPreferredSize(new Dimension(800, 500));
        historyTab.add(historyScrollPane, BorderLayout.CENTER);

        // Thêm 3 tabs
        tabbedPane.addTab("📚 Lịch sử trận đấu", new ImageIcon(), historyTab);
        // tabbedPane.addTab("📱 Từ Client", new ImageIcon(), clientTab);
        // tabbedPane.addTab("🖥️ Admin tự chụp", new ImageIcon(), adminTab);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setRows(8);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log hoạt động"));

        // Layout
        // add(header, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        updateButtonStates();
    }

    private void startReceiver() {
        if (isReceiverRunning)
            return;

        try {
            receiver = new ScreenshotReceiver(this);
            receiver.start();
            isReceiverRunning = true;
            log("ScreenshotReceiver đã khởi động trên port 2346");
            updateButtonStates();
        } catch (Exception ex) {
            log("Lỗi khi khởi động receiver: " + ex.getMessage());
        }
    }

    private void stopReceiver() {
        if (!isReceiverRunning)
            return;

        try {
            if (receiver != null) {
                receiver.stop();
                receiver = null;
            }
            isReceiverRunning = false;
            log("ScreenshotReceiver đã dừng");
            updateButtonStates();
        } catch (Exception ex) {
            log("Lỗi khi dừng receiver: " + ex.getMessage());
        }
    }

    private void updateButtonStates() {
        btnStartReceiver.setEnabled(!isReceiverRunning);
        btnStopReceiver.setEnabled(isReceiverRunning);
        // Bật nút lưu nếu có screenshot từ client hoặc admin
        btnSaveAll.setEnabled(screenshotPanel.getComponentCount() > 0 || adminScreenshotPanel.getComponentCount() > 0);
    }

    private void saveAllScreenshots() {
        try {
            File saveDir = new File("received_screenshots");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            int savedCount = 0;

            // Lưu screenshot từ client
            for (int i = 0; i < screenshotPanel.getComponentCount(); i++) {
                ScreenshotItem item = (ScreenshotItem) screenshotPanel.getComponent(i);
                if (item.getImage() != null) {
                    String fileName = "client_screenshot_" + System.currentTimeMillis() + "_" + i + ".png";
                    File outputFile = new File(saveDir, fileName);
                    ImageIO.write(item.getImage(), "PNG", outputFile);
                    savedCount++;
                }
            }

            // Lưu screenshot admin tự chụp
            for (int i = 0; i < adminScreenshotPanel.getComponentCount(); i++) {
                ScreenshotItem item = (ScreenshotItem) adminScreenshotPanel.getComponent(i);
                if (item.getImage() != null) {
                    String fileName = "admin_screenshot_" + System.currentTimeMillis() + "_" + i + ".png";
                    File outputFile = new File(saveDir, fileName);
                    ImageIO.write(item.getImage(), "PNG", outputFile);
                    savedCount++;
                }
            }

            if (savedCount > 0) {
                JOptionPane.showMessageDialog(this,
                        "Đã lưu " + savedCount + " screenshot vào thư mục: " + saveDir.getAbsolutePath(),
                        "Lưu thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            log("Lỗi khi lưu screenshot: " + ex.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onScreenshotReceived(String fileName, String matchInfo, BufferedImage image,
            InetAddress clientAddress) {
        SwingUtilities.invokeLater(() -> {
            // Tạo item mới để hiển thị screenshot
            ScreenshotItem item = new ScreenshotItem(fileName, matchInfo, image, clientAddress);
            screenshotPanel.add(item);

            // Cập nhật layout
            screenshotPanel.revalidate();
            screenshotPanel.repaint();

            // Log
            log("Nhận screenshot từ " + clientAddress.getHostAddress() + ": " + fileName);
            log("Thông tin: " + matchInfo);

            // Cập nhật button states
            updateButtonStates();
        });
    }

    public void cleanup() {
        stopReceiver();
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
                } catch (Exception e) {
                    log("Không thể đọc file: " + imageFile.getName() + " - " + e.getMessage());
                }
            }

            // Cập nhật layout
            historyScreenshotPanel.revalidate();
            historyScreenshotPanel.repaint();

            log("Đã load " + loadedCount + " ảnh từ thư mục screenshots");

        } catch (Exception e) {
            log("Lỗi khi load ảnh lịch sử: " + e.getMessage());
        }
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
        } catch (Exception e) {
            log("Lỗi khi mở thư mục screenshots: " + e.getMessage());
        }
    }

    /**
     * Chụp ảnh màn hình cho admin
     */
    private void captureAdminScreenshot() {
        try {
            // Chụp toàn bộ màn hình
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // Tạo tên file với timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());
            String fileName = String.format("admin_screenshot_%s.png", timestamp);

            // Tạo thông tin
            String adminInfo = String.format("Admin tự chụp | Thời gian: %s | Kích thước: %dx%d",
                    new SimpleDateFormat("HH:mm:ss").format(new Date()),
                    screenshot.getWidth(), screenshot.getHeight());

            // Tạo item hiển thị
            ScreenshotItem item = new ScreenshotItem(fileName, adminInfo, screenshot, null);
            adminScreenshotPanel.add(item);

            // Cập nhật layout
            adminScreenshotPanel.revalidate();
            adminScreenshotPanel.repaint();

            // Log
            log("Admin đã chụp ảnh màn hình: " + fileName);

            // Hiển thị thông báo
            JOptionPane.showMessageDialog(this,
                    "Đã chụp ảnh màn hình!\n" + fileName,
                    "Chụp ảnh thành công",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            log("Lỗi khi chụp ảnh màn hình: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chụp ảnh: " + ex.getMessage(),
                    "Lỗi chụp ảnh",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Component để hiển thị một screenshot
     */
    private static class ScreenshotItem extends JPanel {
        private final BufferedImage image;
        private final String fileName;
        private final String matchInfo;
        private final InetAddress clientAddress;

        public ScreenshotItem(String fileName, String matchInfo, BufferedImage image, InetAddress clientAddress) {
            this.fileName = fileName;
            this.matchInfo = matchInfo;
            this.image = image;
            this.clientAddress = clientAddress;

            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createTitledBorder("Screenshot từ " + clientAddress.getHostAddress()));

            // Hiển thị ảnh
            ImageIcon icon = new ImageIcon(image);
            Image scaledImage = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);

            // Thông tin
            JTextArea infoArea = new JTextArea();
            infoArea.setText("File: " + fileName + "\n" + matchInfo + "\nClient: " + clientAddress.getHostAddress());
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);
            infoArea.setRows(4);

            add(imageLabel, BorderLayout.CENTER);
            add(infoArea, BorderLayout.SOUTH);
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    /**
     * Component để hiển thị một ảnh lịch sử từ folder screenshots
     */
    private static class HistoryScreenshotItem extends JPanel {
        private final BufferedImage image;
        private final String fileName;
        private final String fileInfo;
        private final File imageFile;

        public HistoryScreenshotItem(String fileName, String fileInfo, BufferedImage image, File imageFile) {
            this.fileName = fileName;
            this.fileInfo = fileInfo;
            this.image = image;
            this.imageFile = imageFile;

            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createTitledBorder(fileName));

            // Hiển thị ảnh với tỷ lệ đúng
            ImageIcon icon = new ImageIcon(image);

            // Tính toán kích thước mới giữ nguyên tỷ lệ
            int maxWidth = 250;
            int maxHeight = 200;
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            double scaleX = (double) maxWidth / originalWidth;
            double scaleY = (double) maxHeight / originalHeight;
            double scale = Math.min(scaleX, scaleY); // Lấy tỷ lệ nhỏ hơn để giữ nguyên tỷ lệ

            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);

            // Thông tin file
            JTextArea infoArea = new JTextArea();
            infoArea.setText(fileInfo);
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);
            infoArea.setRows(3);
            infoArea.setFont(new Font("Monospaced", Font.PLAIN, 10));

            // Nút mở file
            JButton btnOpen = new JButton("📂 Mở file");
            btnOpen.setPreferredSize(new Dimension(100, 25));
            btnOpen.addActionListener(e -> openImageFile());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setOpaque(false);
            buttonPanel.add(btnOpen);

            add(imageLabel, BorderLayout.CENTER);
            add(infoArea, BorderLayout.SOUTH);
            add(buttonPanel, BorderLayout.NORTH);
        }

        private void openImageFile() {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(imageFile);
                }
            } catch (Exception e) {
                System.err.println("Không thể mở file: " + e.getMessage());
            }
        }

        public BufferedImage getImage() {
            return image;
        }
    }
}
