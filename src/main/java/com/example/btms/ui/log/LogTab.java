package com.example.btms.ui.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Tab hiển thị logs từ toàn bộ dự án
 **/
public class LogTab extends JPanel {

    private final JTextArea logArea;
    private JLabel lblStatus;
    private JToggleButton autoScrollToggle;
    private JButton btnClear;
    private JButton btnExport;

    // Capture System.out and System.err
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final ByteArrayOutputStream capturedOutput;
    private final PrintStream capturedOut;

    // Log buffer
    private final ConcurrentLinkedQueue<LogEntry> logBuffer = new ConcurrentLinkedQueue<>();
    private final AtomicInteger logCount = new AtomicInteger(0);

    // UI update timer
    private final Timer updateTimer;

    public LogTab() {
        super(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(new Color(248, 249, 250));

        // Header
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // Log area with modern styling
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBackground(Color.WHITE);
        logArea.setForeground(new Color(33, 37, 41));
        logArea.setCaretColor(new Color(0, 123, 255));
        logArea.setSelectionColor(new Color(0, 123, 255, 50));
        logArea.setSelectedTextColor(new Color(33, 37, 41));

        // Bật word wrap để tránh cuộn ngang
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        scrollPane.getViewport().setBackground(logArea.getBackground());
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = buildStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // Capture system output
        originalOut = System.out;
        originalErr = System.err;
        capturedOutput = new ByteArrayOutputStream();
        capturedOut = new PrintStream(capturedOutput);

        // Start capturing
        startCapturing();

        // UI update timer (every 500ms)
        updateTimer = new Timer(500, e -> updateLogDisplay());
        updateTimer.start();

        // Initial log message
        addLog("INFO", "LogTab", "Tab log đã được khởi tạo");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("📋 Log Toàn Dự Án");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(52, 58, 64));

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.setOpaque(false);

        autoScrollToggle = new JToggleButton("🔒 Tự động cuộn", true);
        autoScrollToggle.setToolTipText("Tự động cuộn xuống khi có log mới (ĐANG BẬT)");
        autoScrollToggle.addActionListener(e -> {
            // Cập nhật text và tooltip dựa trên trạng thái
            if (autoScrollToggle.isSelected()) {
                autoScrollToggle.setText("🔒 Tự động cuộn");
                autoScrollToggle.setToolTipText("Tự động cuộn xuống khi có log mới (ĐANG BẬT)");
            } else {
                autoScrollToggle.setText("🔓 Tự động cuộn");
                autoScrollToggle.setToolTipText("Tự động cuộn xuống khi có log mới (ĐANG TẮT)");
            }
        });
        styleToggleButton(autoScrollToggle, new Color(40, 167, 69), Color.WHITE);

        btnClear = new JButton("🗑️ Xóa");
        btnClear.setToolTipText("Xóa tất cả logs");
        btnClear.addActionListener(e -> clearLogs());
        styleButton(btnClear, new Color(220, 53, 69), Color.WHITE);

        btnExport = new JButton("💾 Xuất");
        btnExport.setToolTipText("Xuất logs ra file");
        btnExport.addActionListener(e -> exportLogs());
        styleButton(btnExport, new Color(0, 123, 255), Color.WHITE);

        // Thêm nút cuộn xuống cuối
        JButton btnScrollToBottom = new JButton("⬇️ Cuộn xuống");
        btnScrollToBottom.setToolTipText("Cuộn xuống cuối logs");
        btnScrollToBottom.addActionListener(e -> scrollToBottom());
        styleButton(btnScrollToBottom, new Color(108, 117, 125), Color.WHITE);

        controls.add(autoScrollToggle);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(btnClear);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(btnExport);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(btnScrollToBottom);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        return header;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        statusBar.setBackground(new Color(248, 249, 250));

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setBorder(new EmptyBorder(0, 0, 0, 0));
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 12f));
        lblStatus.setForeground(new Color(73, 80, 87));

        JLabel lblCount = new JLabel("0 logs");
        lblCount.setBorder(new EmptyBorder(0, 0, 0, 0));
        lblCount.setFont(lblCount.getFont().deriveFont(Font.PLAIN, 11f));
        lblCount.setForeground(new Color(108, 117, 125));

        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblCount, BorderLayout.EAST);

        // Update count label
        Timer countTimer = new Timer(1000, e -> {
            int count = logCount.get();
            lblCount.setText(count + " logs");
        });
        countTimer.start();

        return statusBar;
    }

    private void startCapturing() {
        // Capture System.out
        System.setOut(new PrintStream(capturedOut) {
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
                originalOut.write(buf, off, len);

                String text = new String(buf, off, len);
                if (text.trim().length() > 0) {
                    addLog("OUT", "System.out", text.trim());
                }
            }
        });

        // Capture System.err
        System.setErr(new PrintStream(capturedOut) {
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
                originalErr.write(buf, off, len);

                String text = new String(buf, off, len);
                if (text.trim().length() > 0) {
                    addLog("ERR", "System.err", text.trim());
                }
            }
        });
    }

    public final void addLog(String level, String source, String message) {
        LogEntry entry = new LogEntry(level, source, message);
        logBuffer.offer(entry);
        logCount.incrementAndGet();

        // Limit buffer size
        while (logBuffer.size() > 10000) {
            logBuffer.poll();
        }
    }

    private void updateLogDisplay() {
        if (logBuffer.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        LogEntry entry;

        while ((entry = logBuffer.poll()) != null) {
            sb.append(entry.toString()).append("\n");
        }

        if (sb.length() > 0) {
            SwingUtilities.invokeLater(() -> {
                // Lưu vị trí cuộn hiện tại
                int currentPosition = logArea.getCaretPosition();
                boolean wasAtBottom = isAtBottom();

                logArea.append(sb.toString());

                // Chỉ cuộn xuống nếu auto-scroll được bật HOẶC người dùng đang ở cuối
                if (autoScrollToggle.isSelected() || wasAtBottom) {
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                    // Cuộn viewport xuống cuối
                    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class,
                            logArea);
                    if (scrollPane != null) {
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                    }
                } else {
                    // Giữ nguyên vị trí cuộn nếu auto-scroll tắt và không ở cuối
                    logArea.setCaretPosition(currentPosition);
                }

                lblStatus.setText("Cập nhật: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            });
        }
    }

    private void clearLogs() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
            logBuffer.clear();
            logCount.set(0);
            lblStatus.setText("Đã xóa tất cả logs");
        });
    }

    private void exportLogs() {
        addLog("INFO", "LogTab", "Tính năng xuất logs sẽ được triển khai sau");
    }

    /**
     * Kiểm tra xem người dùng có đang ở cuối log area không
     */
    private boolean isAtBottom() {
        try {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, logArea);
            if (scrollPane != null) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                int currentValue = verticalBar.getValue();
                int visibleAmount = verticalBar.getVisibleAmount();
                int maximum = verticalBar.getMaximum();

                // Nếu vị trí hiện tại + phần hiển thị >= maximum, nghĩa là đang ở cuối
                return (currentValue + visibleAmount) >= (maximum - 5); // Cho phép sai số 5px
            }
        } catch (Exception e) {
            // Fallback: nếu không thể xác định, giả sử đang ở cuối
            addLog("WARN", "LogTab", "Không thể xác định vị trí cuộn: " + e.getMessage());
        }
        return true;
    }

    /**
     * Cuộn xuống cuối logs
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            logArea.setCaretPosition(logArea.getDocument().getLength());
            // Cuộn viewport xuống cuối
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, logArea);
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }
            addLog("INFO", "LogTab", "Đã cuộn xuống cuối logs");
        });
    }

    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.stop();
        }

        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);

        addLog("INFO", "LogTab", "Tab log đã được dọn dẹp");
    }

    // Log entry class
    private static class LogEntry {
        final String level;
        final String source;
        final String message;
        final long timestamp;

        LogEntry(String level, String source, String message) {
            this.level = level;
            this.source = source;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            String timeStr = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(timestamp));
            String sourceStr = String.format("%-18s", source);

            // Color coding for different log levels
            String levelDisplay = switch (level.toUpperCase()) {
                case "INFO" -> "ℹ️ " + level;
                case "ERR" -> "❌ " + level;
                case "OUT" -> "📤 " + level;
                default -> "📝 " + level;
            };

            return String.format("[%s] %s [%s] %s", timeStr, levelDisplay, sourceStr, message);
        }
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void styleToggleButton(JToggleButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}
