package com.example.btms.ui.db;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.btms.config.H2TcpServerConfig;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Panel hiển thị thông tin H2 TCP Server và cho phép copy connection string
 */
public class H2ServerInfoPanel extends JPanel {
    private final H2TcpServerConfig h2ServerConfig;
    private final JLabel statusLabel = new JLabel();
    private final JTextArea connectionInfoArea = new JTextArea(6, 50);
    private final JButton copyUrlButton = new JButton("Copy Connection URL");
    private final JButton copyInfoButton = new JButton("Copy All Info");
    private final JButton refreshButton = new JButton("Refresh");

    public H2ServerInfoPanel(H2TcpServerConfig h2ServerConfig) {
        this.h2ServerConfig = h2ServerConfig;
        initComponents();
        setupLayout();
        setupEventHandlers();
        updateInfo();
    }

    private void initComponents() {
        setOpaque(true);
        putClientProperty(FlatClientProperties.STYLE, "background: @background");
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Status label
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");

        // Connection info text area
        connectionInfoArea.setEditable(false);
        connectionInfoArea.setOpaque(true);
        connectionInfoArea.setBackground(UIManager.getColor("TextField.background"));
        connectionInfoArea.setBorder(BorderFactory.createLoweredBevelBorder());
        connectionInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Buttons
        copyUrlButton.putClientProperty("JButton.buttonType", "default");
        copyInfoButton.putClientProperty("JButton.buttonType", "default");
        refreshButton.putClientProperty("JButton.buttonType", "toolBar");

        // Icons
        try {
            copyUrlButton.setIcon(new FlatSVGIcon("icons/copy.svg", 16, 16));
            copyInfoButton.setIcon(new FlatSVGIcon("icons/copy.svg", 16, 16));
            refreshButton.setIcon(new FlatSVGIcon("icons/refresh.svg", 16, 16));
        } catch (Exception ignore) {
            // Icons not found, continue without icons
        }

        // Tooltips
        copyUrlButton.setToolTipText("Copy connection URL to clipboard");
        copyInfoButton.setToolTipText("Copy all connection information to clipboard");
        refreshButton.setToolTipText("Refresh server status");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("H2 Database TCP Server");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        try {
            titleLabel.setIcon(new FlatSVGIcon("icons/database.svg", 24, 24));
        } catch (Exception ignore) {
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center panel - connection info
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);

        JLabel infoLabel = new JLabel("Connection Information:");
        infoLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +1");

        centerPanel.add(infoLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(connectionInfoArea), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(copyUrlButton);
        buttonPanel.add(copyInfoButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        copyUrlButton.addActionListener(this::copyConnectionUrl);
        copyInfoButton.addActionListener(this::copyAllInfo);
        refreshButton.addActionListener(this::refreshInfo);
    }

    private void copyConnectionUrl(ActionEvent e) {
        try {
            String url = h2ServerConfig.getConnectionUrl();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(url), null);

            showTooltip(copyUrlButton, "✅ Connection URL copied!");
        } catch (Exception ex) {
            showTooltip(copyUrlButton, "❌ Failed to copy URL");
        }
    }

    private void copyAllInfo(ActionEvent e) {
        try {
            String info = connectionInfoArea.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(info), null);

            showTooltip(copyInfoButton, "✅ All info copied!");
        } catch (Exception ex) {
            showTooltip(copyInfoButton, "❌ Failed to copy info");
        }
    }

    private void refreshInfo(ActionEvent e) {
        updateInfo();
        // Cũng hiển thị đầy đủ thông tin trong console log
        h2ServerConfig.showConnectionInfo();
        showTooltip(refreshButton, "✅ Refreshed!");
    }

    private void showTooltip(JComponent component, String message) {
        // Simple tooltip feedback
        String originalTooltip = component.getToolTipText();
        component.setToolTipText(message);

        Timer timer = new Timer(2000, evt -> component.setToolTipText(originalTooltip));
        timer.setRepeats(false);
        timer.start();
    }

    public void updateInfo() {
        SwingUtilities.invokeLater(() -> {
            boolean isRunning = h2ServerConfig.isServerRunning();

            if (isRunning) {
                statusLabel.setText("🟢 Server Running");
                statusLabel.setForeground(new Color(0, 150, 0));
            } else {
                statusLabel.setText("🔴 Server Stopped");
                statusLabel.setForeground(new Color(180, 0, 0));
            }

            String info = h2ServerConfig.getConnectionInfo();
            if (isRunning) {
                info += "\n\n📌 Hướng dẫn kết nối từ máy khác:";
                info += "\n1. Cài đặt H2 Database hoặc client JDBC";
                info += "\n2. Sử dụng thông tin kết nối ở trên";
                info += "\n3. Đảm bảo firewall cho phép port " + h2ServerConfig.getServerPort();
                info += "\n4. Database file sẽ được tạo tự động nếu chưa tồn tại";
            }

            connectionInfoArea.setText(info);
            connectionInfoArea.setCaretPosition(0);
        });
    }
}