package com.example.btms.service.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.btms.service.threading.BackgroundTaskManager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * 📊 Performance Monitoring Service cho Java 21
 * 
 * Theo dõi hiệu năng hệ thống và thread pools
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringService.class);

    @Autowired
    private BackgroundTaskManager taskManager;

    private ScheduledFuture<?> monitoringTask;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // Performance metrics
    private volatile int peakThreadCount = 0;
    private volatile long maxMemoryUsed = 0;

    @PostConstruct
    public void startMonitoring() {
        log.info("🚀 Starting Performance Monitoring Service");

        // Start monitoring task every 30 seconds
        monitoringTask = taskManager.schedulePeriodicTask(
                this::collectAndLogMetrics,
                10, // Initial delay
                30, // Period
                TimeUnit.SECONDS);

        // Start memory cleanup task
        taskManager.startMemoryCleanupTask();
    }

    @PreDestroy
    public void stopMonitoring() {
        if (monitoringTask != null && !monitoringTask.isCancelled()) {
            monitoringTask.cancel(true);
            log.info("📊 Performance monitoring stopped");
        }
    }

    /**
     * 📈 Thu thập và log metrics
     */
    private void collectAndLogMetrics() {
        try {
            // Memory metrics
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();

            // Thread metrics
            int currentThreadCount = threadBean.getThreadCount();
            int daemonThreadCount = threadBean.getDaemonThreadCount();

            // Update peak values
            if (currentThreadCount > peakThreadCount) {
                peakThreadCount = currentThreadCount;
            }
            if (heapUsed > maxMemoryUsed) {
                maxMemoryUsed = heapUsed;
            }

            // Calculate percentages
            double heapUsagePercent = (double) heapUsed / heapMax * 100;

            // Log metrics periodically (every 5 minutes)
            if (System.currentTimeMillis() % (5 * 60 * 1000) < 30000) { // Within 30s window
                log.info("📊 Performance Metrics:");
                log.info("  💾 Heap Memory: {}MB / {}MB ({}%)",
                        heapUsed / 1024 / 1024,
                        heapMax / 1024 / 1024,
                        String.format("%.1f", heapUsagePercent));
                log.info("  🧵 Threads: {} active ({} daemon), peak: {}",
                        currentThreadCount, daemonThreadCount, peakThreadCount);
                log.info("  📈 Max Memory Used: {}MB", maxMemoryUsed / 1024 / 1024);
            }

            // Warning thresholds
            if (heapUsagePercent > 85) {
                log.warn("⚠️ High memory usage: {}%", String.format("%.1f", heapUsagePercent));
            }

            if (currentThreadCount > 100) {
                log.warn("⚠️ High thread count: {}", currentThreadCount);
            }

        } catch (Exception e) {
            log.warn("Error collecting performance metrics: {}", e.getMessage());
        }
    }

    /**
     * 🎯 Get current performance snapshot
     */
    public PerformanceSnapshot getSnapshot() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        int threadCount = threadBean.getThreadCount();

        return new PerformanceSnapshot(
                heapUsed,
                heapMax,
                threadCount,
                peakThreadCount,
                maxMemoryUsed);
    }

    /**
     * 🔄 Force garbage collection nếu cần thiết
     */
    public void suggestGarbageCollection() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        double usage = (double) heapUsed / heapMax * 100;

        if (usage > 75) {
            log.info("🧹 Suggesting garbage collection (memory usage: {}%)",
                    String.format("%.1f", usage));
            System.gc();
        }
    }

    /**
     * 📊 Performance snapshot data class
     */
    public static class PerformanceSnapshot {
        public final long heapUsed;
        public final long heapMax;
        public final int currentThreadCount;
        public final int peakThreadCount;
        public final long maxMemoryUsed;

        public PerformanceSnapshot(long heapUsed, long heapMax, int currentThreadCount,
                int peakThreadCount, long maxMemoryUsed) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.currentThreadCount = currentThreadCount;
            this.peakThreadCount = peakThreadCount;
            this.maxMemoryUsed = maxMemoryUsed;
        }

        public double getHeapUsagePercent() {
            return (double) heapUsed / heapMax * 100;
        }

        @Override
        public String toString() {
            return String.format("Memory: %dMB/%dMB (%.1f%%), Threads: %d (peak: %d)",
                    heapUsed / 1024 / 1024,
                    heapMax / 1024 / 1024,
                    getHeapUsagePercent(),
                    currentThreadCount,
                    peakThreadCount);
        }
    }
}