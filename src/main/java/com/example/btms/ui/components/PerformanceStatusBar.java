package com.example.btms.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.btms.service.monitoring.PerformanceMonitoringService;
import com.example.btms.service.threading.BackgroundTaskManager;
import com.example.btms.util.threading.SwingThreadingUtils;

/**
 * 📊 Enhanced Performance Status Bar cho Java 21
 * 
 * Hiển thị real-time performance metrics trong status bar
 */
public class PerformanceStatusBar extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(PerformanceStatusBar.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    private final JLabel memoryLabel;
    private final JLabel threadLabel;
    private final JLabel cpuLabel;
    private final JProgressBar memoryProgressBar;

    private PerformanceMonitoringService performanceService;
    private BackgroundTaskManager taskManager;
    private ScheduledFuture<?> updateTask;

    public PerformanceStatusBar() {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        setBorder(BorderFactory.createLoweredBevelBorder());
        setPreferredSize(new Dimension(0, 25));

        // Memory usage progress bar
        memoryProgressBar = new JProgressBar(0, 100);
        memoryProgressBar.setPreferredSize(new Dimension(80, 16));
        memoryProgressBar.setStringPainted(true);
        memoryProgressBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));

        // Labels
        memoryLabel = createStatusLabel("Memory: --");
        threadLabel = createStatusLabel("Threads: --");
        cpuLabel = createStatusLabel("Load: --");

        // Add components
        add(cpuLabel);
        add(new JLabel("|"));
        add(threadLabel);
        add(new JLabel("|"));
        add(memoryLabel);
        add(memoryProgressBar);

        // Initialize with placeholder values
        updateDisplay(null);
    }

    /**
     * 🚀 Initialize với performance services
     */
    public void initialize(PerformanceMonitoringService performanceService,
            BackgroundTaskManager taskManager) {
        this.performanceService = performanceService;
        this.taskManager = taskManager;

        // Start periodic updates
        startPeriodicUpdates();
    }

    /**
     * 🔄 Bắt đầu cập nhật periodic
     */
    private void startPeriodicUpdates() {
        if (taskManager != null) {
            updateTask = taskManager.schedulePeriodicTask(
                    this::updatePerformanceDisplay,
                    1, // Initial delay
                    2, // Update every 2 seconds
                    TimeUnit.SECONDS);
            log.debug("📊 Performance status bar updates started");
        }
    }

    /**
     * 📈 Cập nhật display với performance data
     */
    private void updatePerformanceDisplay() {
        try {
            if (performanceService != null) {
                // Get performance snapshot
                PerformanceMonitoringService.PerformanceSnapshot snapshot = performanceService.getSnapshot();

                // Update UI trên EDT
                SwingThreadingUtils.runOnEDT(() -> updateDisplay(snapshot));
            }
        } catch (Exception e) {
            log.warn("Error updating performance display: {}", e.getMessage());
        }
    }

    /**
     * 🎨 Update UI elements
     */
    private void updateDisplay(PerformanceMonitoringService.PerformanceSnapshot snapshot) {
        if (snapshot != null) {
            // Memory usage
            double memoryPercent = snapshot.getHeapUsagePercent();
            long memoryMB = snapshot.heapUsed / 1024 / 1024;
            long maxMemoryMB = snapshot.heapMax / 1024 / 1024;

            memoryLabel.setText(String.format("Memory: %dMB/%dMB", memoryMB, maxMemoryMB));

            memoryProgressBar.setValue((int) memoryPercent);
            memoryProgressBar.setString(DECIMAL_FORMAT.format(memoryPercent) + "%");

            // Color coding for memory usage
            if (memoryPercent > 85) {
                memoryProgressBar.setForeground(Color.RED);
            } else if (memoryPercent > 70) {
                memoryProgressBar.setForeground(Color.ORANGE);
            } else {
                memoryProgressBar.setForeground(Color.GREEN);
            }

            // Thread count
            threadLabel.setText(String.format("Threads: %d", snapshot.currentThreadCount));

            // Thread count color coding
            if (snapshot.currentThreadCount > 100) {
                threadLabel.setForeground(Color.RED);
            } else if (snapshot.currentThreadCount > 50) {
                threadLabel.setForeground(Color.ORANGE);
            } else {
                threadLabel.setForeground(Color.BLACK);
            }

            // CPU load (simplified)
            Runtime runtime = Runtime.getRuntime();
            int processors = runtime.availableProcessors();
            cpuLabel.setText(String.format("CPU: %d cores", processors));

        } else {
            // Fallback display
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            memoryLabel.setText(String.format("Memory: %dMB", usedMemory / 1024 / 1024));
            threadLabel.setText("Threads: --");
            cpuLabel.setText("Load: --");

            memoryProgressBar.setValue(0);
            memoryProgressBar.setString("--");
        }
    }

    /**
     * 🎯 Create styled label
     */
    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        return label;
    }

    /**
     * 🧹 Cleanup resources
     */
    public void cleanup() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel(true);
            log.debug("📊 Performance status bar updates stopped");
        }
    }

    /**
     * 🚀 Force update display ngay lập tức
     */
    public void forceUpdate() {
        updatePerformanceDisplay();
    }
}