package com.example.btms.ui.log;

import com.example.btms.util.log.Log;
import com.example.btms.util.monitor.ThreadPoolMonitor;
import com.example.btms.util.ui.IconUtil;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog hiển thị System Logs Viewer với H2 TCP Server và Thread Pool
 * monitoring
 */
public class LogViewerDialog extends JDialog {

    private final LogViewerPanel logViewerPanel;
    private final ThreadPoolMonitor threadMonitor;
    private final Log sharedLog;

    public LogViewerDialog(Window parent) {
        super(parent, "System Logs Viewer", ModalityType.MODELESS);

        // Shared log instance for both H2 and thread monitoring
        this.sharedLog = new Log();
        this.threadMonitor = new ThreadPoolMonitor(sharedLog);
        this.logViewerPanel = new LogViewerPanel();

        // Connect the log viewer to shared log
        this.sharedLog.attach(this.logViewerPanel.getLogArea());

        initDialog();
        setupLayout();
        setupWindowListener();

        pack();
        setLocationRelativeTo(parent);

        // Start thread monitoring
        startMonitoring();
    }

    private void initDialog() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(true);

        // Apply app icons
        IconUtil.applyTo(this);

        // Set minimum size
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1000, 700));
    }

    private void setupLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rootPanel.putClientProperty(FlatClientProperties.STYLE, "background: @background");

        // Add main log viewer panel
        rootPanel.add(logViewerPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = createControlPanel();
        rootPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Thread monitoring controls
        JButton threadInfoButton = new JButton("Thread Info");
        threadInfoButton.putClientProperty("JButton.buttonType", "default");
        threadInfoButton.setToolTipText("Log current thread information");
        threadInfoButton.addActionListener(e -> threadMonitor.logThreadInfo());

        JButton detailedThreadButton = new JButton("Detailed Threads");
        detailedThreadButton.putClientProperty("JButton.buttonType", "default");
        detailedThreadButton.setToolTipText("Log detailed thread information");
        detailedThreadButton.addActionListener(e -> threadMonitor.logDetailedThreadInfo());

        // H2 server info button
        JButton h2InfoButton = new JButton("H2 Server Info");
        h2InfoButton.putClientProperty("JButton.buttonType", "default");
        h2InfoButton.setToolTipText("Log H2 TCP Server information");
        h2InfoButton.addActionListener(e -> logH2ServerInfo());

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.putClientProperty("JButton.buttonType", "default");
        closeButton.addActionListener(e -> closeDialog());

        panel.add(threadInfoButton);
        panel.add(detailedThreadButton);
        panel.add(h2InfoButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(closeButton);

        return panel;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
    }

    private void startMonitoring() {
        // Start thread monitoring with 10-second interval
        threadMonitor.startMonitoring(10);

        // Log initial info
        sharedLog.logTs("🚀 System Logs Viewer started");
        sharedLog.logTs("📊 Monitoring: Thread Pool + H2 TCP Server");
        threadMonitor.logThreadInfo();
    }

    private void logH2ServerInfo() {
        try {
            // Try to get H2 server info from application context
            // This would need to be adapted based on how you access H2TcpServerConfig
            sharedLog.log("📋 H2 TCP Server Status:");
            sharedLog.log("💡 Note: H2 server info integration pending - add ApplicationContext access");
            sharedLog.log("🔍 Check MainFrame status bar for current H2 server status");
        } catch (Exception e) {
            sharedLog.log("❌ Error getting H2 server info: %s", e.getMessage());
        }
    }

    private void closeDialog() {
        // Stop monitoring
        threadMonitor.shutdown();

        // Dispose panel resources
        logViewerPanel.dispose();

        // Log shutdown
        sharedLog.log("🔚 System Logs Viewer closed");

        // Close dialog
        dispose();
    }

    /**
     * Get the log area for external access
     */
    public JTextArea getLogArea() {
        return logViewerPanel.getLogArea();
    }

    /**
     * Get the shared log instance
     */
    public Log getSharedLog() {
        return sharedLog;
    }

    /**
     * Get the thread monitor
     */
    public ThreadPoolMonitor getThreadMonitor() {
        return threadMonitor;
    }

    /**
     * Refresh the log viewer
     */
    public void refreshLogs() {
        logViewerPanel.refreshAll();
    }
}