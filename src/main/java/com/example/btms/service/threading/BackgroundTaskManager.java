package com.example.btms.service.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * 🚀 Enhanced Background Task Manager với Java 21 optimizations
 * 
 * Quản lý các tác vụ background một cách hiệu quả và mượt mà hơn
 */
@Service
public class BackgroundTaskManager {

    private static final Logger log = LoggerFactory.getLogger(BackgroundTaskManager.class);

    @Autowired
    @Qualifier("virtualThreadExecutor")
    private ExecutorService virtualExecutor;

    @Autowired
    @Qualifier("ioIntensiveExecutor")
    private ExecutorService ioExecutor;

    @Autowired
    @Qualifier("cpuIntensiveExecutor")
    private ExecutorService cpuExecutor;

    @Autowired
    @Qualifier("scheduledExecutor")
    private ScheduledExecutorService scheduledExecutor;

    @PostConstruct
    public void init() {
        log.info("🚀 BackgroundTaskManager initialized with enhanced thread pools");
    }

    /**
     * 🌐 Thực thi SSE broadcast task (I/O intensive)
     */
    public CompletableFuture<Void> executeSseBroadcast(Runnable task) {
        return CompletableFuture.runAsync(task, virtualExecutor)
                .exceptionally(throwable -> {
                    log.warn("SSE broadcast task failed: {}", throwable.getMessage());
                    return null;
                });
    }

    /**
     * 💾 Thực thi database operation (I/O intensive)
     */
    public <T> CompletableFuture<T> executeDatabaseTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, ioExecutor)
                .exceptionally(throwable -> {
                    log.warn("Database task failed: {}", throwable.getMessage());
                    return null;
                });
    }

    /**
     * 🖼️ Thực thi CPU-intensive task (PDF generation, image processing)
     */
    public <T> CompletableFuture<T> executeCpuIntensiveTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, cpuExecutor)
                .exceptionally(throwable -> {
                    log.warn("CPU intensive task failed: {}", throwable.getMessage());
                    return null;
                });
    }

    /**
     * ⏰ Schedule periodic task
     */
    public ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.warn("Scheduled task failed: {}", e.getMessage());
            }
        }, initialDelay, period, unit);
    }

    /**
     * 🎯 Schedule one-time delayed task
     */
    public ScheduledFuture<?> scheduleDelayedTask(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.warn("Delayed task failed: {}", e.getMessage());
            }
        }, delay, unit);
    }

    /**
     * 🔄 Chạy task với retry logic
     */
    public CompletableFuture<Void> executeWithRetry(Runnable task, int maxRetries) {
        return CompletableFuture.runAsync(() -> {
            Exception lastException = null;
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    task.run();
                    return; // Success
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < maxRetries) {
                        log.warn("Task attempt {} failed, retrying... Error: {}", attempt + 1, e.getMessage());
                        try {
                            Thread.sleep(100 * (attempt + 1)); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
            log.error("Task failed after {} attempts", maxRetries + 1, lastException);
        }, virtualExecutor);
    }

    /**
     * 🧹 Cleanup periodic task để tối ưu memory
     */
    public void startMemoryCleanupTask() {
        schedulePeriodicTask(() -> {
            try {
                // Suggest GC nếu memory usage cao
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

                if (memoryUsagePercent > 80) {
                    log.info("🧹 High memory usage detected ({}%), suggesting GC",
                            String.format("%.1f", memoryUsagePercent));
                    System.gc();
                }
            } catch (Exception e) {
                log.warn("Memory cleanup task error: {}", e.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS); // Check every minute after 30s initial delay
    }

    /**
     * 📊 Log thread pool statistics
     */
    public void logThreadPoolStatistics() {
        log.info("📊 Thread Pool Statistics:");
        log.info("  - Available processors: {}", Runtime.getRuntime().availableProcessors());

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("  - Memory usage: {}MB / {}MB ({}%)",
                usedMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                String.format("%.1f", (double) usedMemory / maxMemory * 100));
    }
}