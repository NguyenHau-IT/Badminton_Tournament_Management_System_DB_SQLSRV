package com.example.btms.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * 🚀 Java 21 Enhanced Thread Management Configuration
 * 
 * Tận dụng Virtual Threads (Project Loom) cho hiệu năng tốt hơn
 * và quản lý thread pool centralized
 */
@Configuration
public class ThreadConfig {

    private static final Logger log = LoggerFactory.getLogger(ThreadConfig.class);

    // Virtual Thread Executors (Java 21 feature)
    private ExecutorService virtualThreadExecutor;
    private ExecutorService ioIntensiveExecutor;
    private ExecutorService cpuIntensiveExecutor;
    private ScheduledExecutorService scheduledExecutor;

    /**
     * 🌟 Enhanced Thread Executor - BOUNDED để tránh thread explosion
     * Tối ưu cho các tác vụ I/O intensive và SSE broadcasting
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        int maxThreads = Math.max(50, Runtime.getRuntime().availableProcessors() * 4);
        log.info("🚀 Creating Enhanced Thread Executor with max {} threads (Java 21 optimized)", maxThreads);
        // Sử dụng bounded thread pool thay vì cached để tránh thread explosion
        this.virtualThreadExecutor = Executors.newFixedThreadPool(maxThreads,
                new NamedThreadFactory("Enhanced"));
        return this.virtualThreadExecutor;
    }

    /**
     * 🔄 I/O Intensive Thread Pool - Optimized size
     * Cho database operations, file I/O, network requests
     */
    @Bean(name = "ioIntensiveExecutor")
    public ExecutorService ioIntensiveExecutor() {
        int ioThreads = Math.min(20, Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
        log.info("💾 Creating I/O Intensive Thread Pool with {} threads", ioThreads);
        this.ioIntensiveExecutor = Executors.newFixedThreadPool(ioThreads,
                new NamedThreadFactory("IO-Intensive"));
        return this.ioIntensiveExecutor;
    }

    /**
     * ⚡ CPU Intensive Thread Pool
     * Cho PDF generation, image processing, calculations
     */
    @Bean(name = "cpuIntensiveExecutor")
    public ExecutorService cpuIntensiveExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        log.info("🧮 Creating CPU Intensive Thread Pool with {} threads", cores);
        this.cpuIntensiveExecutor = Executors.newFixedThreadPool(
                cores,
                new NamedThreadFactory("CPU-Intensive"));
        return this.cpuIntensiveExecutor;
    }

    /**
     * ⏰ Scheduled Task Executor
     * Cho periodic tasks, timeouts, cleanup operations
     */
    @Bean(name = "scheduledExecutor")
    public ScheduledExecutorService scheduledExecutor() {
        log.info("⏰ Creating Scheduled Executor Service");
        this.scheduledExecutor = Executors.newScheduledThreadPool(
                4,
                new NamedThreadFactory("Scheduled"));
        return this.scheduledExecutor;
    }

    /**
     * 🧵 Custom Thread Factory với naming pattern
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = "BTMS-" + namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 🧹 Cleanup resources khi shutdown
     */
    @PreDestroy
    public void cleanup() {
        log.info("🧹 Shutting down thread pools...");

        if (virtualThreadExecutor != null && !virtualThreadExecutor.isShutdown()) {
            virtualThreadExecutor.shutdown();
            log.info("✅ Virtual Thread Executor shut down");
        }

        if (ioIntensiveExecutor != null && !ioIntensiveExecutor.isShutdown()) {
            ioIntensiveExecutor.shutdown();
            log.info("✅ I/O Intensive Executor shut down");
        }

        if (cpuIntensiveExecutor != null && !cpuIntensiveExecutor.isShutdown()) {
            cpuIntensiveExecutor.shutdown();
            log.info("✅ CPU Intensive Executor shut down");
        }

        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
            log.info("✅ Scheduled Executor shut down");
        }
    }

    /**
     * 📊 Utility method để log thread pool status
     */
    public void logThreadPoolStatus() {
        log.info("📊 Thread Pool Status:");
        log.info("  - Available processors: {}", Runtime.getRuntime().availableProcessors());
        log.info("  - Virtual Thread Executor: {}",
                virtualThreadExecutor != null ? "Active" : "Not initialized");
        log.info("  - I/O Intensive Pool: {}",
                ioIntensiveExecutor != null && !ioIntensiveExecutor.isShutdown() ? "Active" : "Inactive");
        log.info("  - CPU Intensive Pool: {}",
                cpuIntensiveExecutor != null && !cpuIntensiveExecutor.isShutdown() ? "Active" : "Inactive");
        log.info("  - Scheduled Pool: {}",
                scheduledExecutor != null && !scheduledExecutor.isShutdown() ? "Active" : "Inactive");
    }
}