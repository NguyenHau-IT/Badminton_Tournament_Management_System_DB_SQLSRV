package com.example.btms.ui.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import com.example.btms.util.log.Log;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Panel hiển thị log của H2 TCP Server và Thread Pool với auto-refresh
 */
public class LogViewerPanel extends JPanel {

    private final JTextArea logArea;
    private final JButton refreshButton;
    private final JButton clearButton;
    private final JButton autoRefreshButton;
    private final JLabel statusLabel;
    private final JScrollPane scrollPane;
    private final Log logUtil; // Instance of Log utility

    private ScheduledExecutorService autoRefreshService;
    private boolean autoRefreshEnabled = false;
    private int autoRefreshInterval = 5; // seconds

    public LogViewerPanel() {
        this.logArea = new JTextArea(25, 80);
        this.refreshButton = new JButton("Refresh");
        this.clearButton = new JButton("Clear");
        this.autoRefreshButton = new JButton("Auto Refresh: OFF");
        this.statusLabel = new JLabel("Ready");
        this.scrollPane = new JScrollPane(logArea);
        this.logUtil = new Log(); // Create Log instance

        initComponents();
        setupLayout();
        setupEventHandlers();
        loadLogs();
    }

    private void initComponents() {
        // Panel styling
        setOpaque(true);
        putClientProperty(FlatClientProperties.STYLE, "background: @background");
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Log area styling
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Auto-scroll to bottom
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Scroll pane styling
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Logs"));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Button styling
        refreshButton.putClientProperty("JButton.buttonType", "default");
        clearButton.putClientProperty("JButton.buttonType", "default");
        autoRefreshButton.putClientProperty("JButton.buttonType", "default");

        // Status label styling
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold");
        statusLabel.setForeground(new Color(100, 100, 100));

        // Icons
        try {
            refreshButton.setIcon(new FlatSVGIcon("icons/refresh.svg", 16, 16));
            clearButton.setIcon(new FlatSVGIcon("icons/clear.svg", 16, 16));
            autoRefreshButton.setIcon(new FlatSVGIcon("icons/clock.svg", 16, 16));
        } catch (Exception ignore) {
            // Icons not found, continue without icons
        }

        // Tooltips
        refreshButton.setToolTipText("Refresh logs manually");
        clearButton.setToolTipText("Clear log display");
        autoRefreshButton.setToolTipText("Toggle auto-refresh every " + autoRefreshInterval + " seconds");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Header panel with title and controls
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("System Logs Viewer");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        try {
            titleLabel.setIcon(new FlatSVGIcon("icons/log.svg", 24, 24));
        } catch (Exception ignore) {
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(autoRefreshButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with status
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        footerPanel.add(statusLabel, BorderLayout.WEST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(this::refreshLogs);
        clearButton.addActionListener(this::clearLogs);
        autoRefreshButton.addActionListener(this::toggleAutoRefresh);
    }

    private void refreshLogs(ActionEvent e) {
        loadLogs();
    }

    private void clearLogs(ActionEvent e) {
        logArea.setText("");
        statusLabel.setText("Logs cleared");

        // Clear the log utility's content as well
        logUtil.clearConsole();

        // Show confirmation
        Timer timer = new Timer(2000, evt -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    private void toggleAutoRefresh(ActionEvent e) {
        if (autoRefreshEnabled) {
            stopAutoRefresh();
        } else {
            startAutoRefresh();
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshService == null || autoRefreshService.isShutdown()) {
            autoRefreshService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "LogViewer-AutoRefresh");
                t.setDaemon(true);
                return t;
            });
        }

        autoRefreshService.scheduleAtFixedRate(
                this::loadLogs,
                autoRefreshInterval,
                autoRefreshInterval,
                TimeUnit.SECONDS);

        autoRefreshEnabled = true;
        autoRefreshButton.setText("Auto Refresh: ON");
        autoRefreshButton.setForeground(new Color(0, 150, 0));
        statusLabel.setText("Auto-refresh enabled (" + autoRefreshInterval + "s interval)");

        logToConsole("🔄 Auto-refresh enabled with %d seconds interval", autoRefreshInterval);
    }

    private void stopAutoRefresh() {
        if (autoRefreshService != null && !autoRefreshService.isShutdown()) {
            autoRefreshService.shutdown();
        }

        autoRefreshEnabled = false;
        autoRefreshButton.setText("Auto Refresh: OFF");
        autoRefreshButton.setForeground(UIManager.getColor("Button.foreground"));
        statusLabel.setText("Auto-refresh disabled");

        logToConsole("⏹️ Auto-refresh disabled");
    }

    private void loadLogs() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Get logs from Log utility
                String allLogs = logUtil.getAllLogs();

                if (allLogs != null && !allLogs.trim().isEmpty()) {
                    logArea.setText(allLogs);

                    // Auto-scroll to bottom
                    logArea.setCaretPosition(logArea.getDocument().getLength());

                    // Update status
                    int lineCount = allLogs.split("\n").length;
                    statusLabel.setText("Loaded " + lineCount + " log entries");
                } else {
                    logArea.setText("No logs available");
                    statusLabel.setText("No logs found");
                }

            } catch (Exception ex) {
                logArea.setText("Error loading logs: " + ex.getMessage());
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
    }

    private void logToConsole(String message, Object... args) {
        logUtil.log("📊 [LOG-VIEWER] " + String.format(message, args));
    }

    /**
     * Public API for refresh from external components
     */
    public void refreshAll() {
        loadLogs();
    }

    /**
     * Get the log text area for external access
     */
    public JTextArea getLogArea() {
        return logArea;
    }

    /**
     * Cleanup resources when panel is disposed
     */
    public void dispose() {
        stopAutoRefresh();
        if (autoRefreshService != null) {
            try {
                autoRefreshService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logToConsole("🗑️ Log viewer disposed");
    }
}