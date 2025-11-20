package com.example.btms.ui.db;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * Dialog hiển thị log khi tạo database mới
 */
public class DatabaseCreationLogDialog extends JDialog {
    private final JTextArea logArea;
    private final JScrollPane scrollPane;
    private final JButton closeButton;
    private final JProgressBar progressBar;

    public DatabaseCreationLogDialog(Window parent, String dbName) {
        super(parent, "Đang tạo database: " + dbName, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tạo text area để hiển thị log
        logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);

        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Panel phía dưới với progress bar và button
        JPanel bottomPanel = new JPanel(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Đang tạo database...");
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Đóng");
        closeButton.setEnabled(false); // Disable until completion
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);

        // Thêm log đầu tiên
        appendLog("🔄 Bắt đầu tạo database: " + dbName);
        appendLog("📁 Kiểm tra thư mục database...");
    }

    /**
     * Thêm dòng log mới
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Báo hoàn thành thành công
     */
    public void markCompleted(boolean success) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            if (success) {
                progressBar.setValue(100);
                progressBar.setString("✅ Hoàn thành thành công");
                progressBar.setForeground(new Color(46, 204, 113));
                appendLog("");
                appendLog("✅ Database đã được tạo thành công!");
                appendLog("💡 Bạn có thể đóng cửa sổ này và chọn 'Sử dụng local / mạng LAN' để kết nối.");
            } else {
                progressBar.setValue(0);
                progressBar.setString("❌ Tạo database thất bại");
                progressBar.setForeground(new Color(231, 76, 60));
                appendLog("");
                appendLog("❌ Có lỗi xảy ra khi tạo database!");
            }
            closeButton.setEnabled(true);
        });
    }

    /**
     * Set task để có thể cancel nếu cần
     */
    public void setCreationTask(CompletableFuture<Void> task) {
    }
}