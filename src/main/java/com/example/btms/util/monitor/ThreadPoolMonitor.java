package com.example.btms.util.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.btms.util.log.Log;

/**
 * Thread Pool Monitor để log thông tin thread pool và system threads
 */
public class ThreadPoolMonitor {

    private final Log logger;
    private ScheduledExecutorService monitorService;
    private boolean monitoring = false;

    public ThreadPoolMonitor(Log logger) {
        this.logger = logger;
    }

    /**
     * Bắt đầu monitoring thread pool với interval định trước
     */
    public void startMonitoring(int intervalSeconds) {
        if (monitoring) {
            return;
        }

        monitorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ThreadPoolMonitor");
            t.setDaemon(true);
            return t;
        });

        monitorService.scheduleAtFixedRate(
                this::logThreadInfo,
                0,
                intervalSeconds,
                TimeUnit.SECONDS);

        monitoring = true;
        logger.logTs("🔄 Thread Pool Monitoring started (interval: %d seconds)", intervalSeconds);
    }

    /**
     * Dừng monitoring
     */
    public void stopMonitoring() {
        if (!monitoring || monitorService == null) {
            return;
        }

        monitorService.shutdown();
        try {
            if (!monitorService.awaitTermination(2, TimeUnit.SECONDS)) {
                monitorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        monitoring = false;
        logger.logTs("⏹️ Thread Pool Monitoring stopped");
    }

    /**
     * Log thông tin thread hiện tại
     */
    public void logThreadInfo() {
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            // Basic thread info
            int activeThreads = threadBean.getThreadCount();
            int peakThreads = threadBean.getPeakThreadCount();
            int daemonThreads = threadBean.getDaemonThreadCount();

            logger.log("🧵 [THREAD-MONITOR] Active: %d | Peak: %d | Daemon: %d",
                    activeThreads, peakThreads, daemonThreads);

            // Memory info
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory() / (1024 * 1024);

            logger.log("💾 [MEMORY-MONITOR] Used: %d MB | Free: %d MB | Total: %d MB | Max: %d MB",
                    usedMemory, freeMemory, totalMemory, maxMemory);

            // GC info if available
            try {
                java.lang.management.GarbageCollectorMXBean[] gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
                        .toArray(new java.lang.management.GarbageCollectorMXBean[0]);

                for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
                    if (gcBean.getCollectionCount() > 0) {
                        logger.log("🗑️ [GC-MONITOR] %s: Collections=%d | Time=%dms",
                                gcBean.getName(), gcBean.getCollectionCount(), gcBean.getCollectionTime());
                    }
                }
            } catch (Exception gcEx) {
                // GC info not available, skip
            }

        } catch (Exception e) {
            logger.log("❌ [THREAD-MONITOR] Error getting thread info: %s", e.getMessage());
        }
    }

    /**
     * Log chi tiết về một ThreadPoolExecutor cụ thể
     */
    public void logThreadPoolDetails(String name, ThreadPoolExecutor executor) {
        if (executor == null) {
            logger.log("❌ [THREAD-POOL] %s: null executor", name);
            return;
        }

        try {
            int active = executor.getActiveCount();
            int core = executor.getCorePoolSize();
            int max = executor.getMaximumPoolSize();
            int current = executor.getPoolSize();
            long completed = executor.getCompletedTaskCount();
            long total = executor.getTaskCount();
            int queued = executor.getQueue().size();

            logger.log("🏊 [THREAD-POOL] %s: Active=%d/%d | Pool=%d/%d | Completed=%d/%d | Queued=%d",
                    name, active, core, current, max, completed, total, queued);

            // Check if pool is busy
            if (active >= core * 0.8) {
                logger.log("⚠️ [THREAD-POOL] %s: High utilization (%.1f%%)", name, (double) active / core * 100);
            }

            if (queued > 10) {
                logger.log("⚠️ [THREAD-POOL] %s: High queue size: %d", name, queued);
            }

        } catch (Exception e) {
            logger.log("❌ [THREAD-POOL] Error monitoring %s: %s", name, e.getMessage());
        }
    }

    /**
     * Log thông tin tất cả threads hiện tại (chi tiết)
     */
    public void logDetailedThreadInfo() {
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long[] threadIds = threadBean.getAllThreadIds();

            logger.log("📊 [THREAD-DETAIL] Total threads: %d", threadIds.length);

            // Group threads by name pattern
            java.util.Map<String, Integer> threadGroups = new java.util.HashMap<>();

            for (long threadId : threadIds) {
                try {
                    java.lang.management.ThreadInfo info = threadBean.getThreadInfo(threadId);
                    if (info != null) {
                        String name = info.getThreadName();
                        String group = getThreadGroup(name);
                        threadGroups.merge(group, 1, Integer::sum);
                    }
                } catch (Exception ignore) {
                    // Thread might have been terminated
                }
            }

            // Log grouped results
            threadGroups.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> {
                        logger.log("📈 [THREAD-GROUP] %s: %d threads", entry.getKey(), entry.getValue());
                    });

        } catch (Exception e) {
            logger.log("❌ [THREAD-DETAIL] Error: %s", e.getMessage());
        }
    }

    private String getThreadGroup(String threadName) {
        if (threadName == null)
            return "Unknown";

        if (threadName.startsWith("pool-"))
            return "ThreadPool";
        if (threadName.startsWith("Timer-"))
            return "Timer";
        if (threadName.startsWith("AWT-"))
            return "AWT/Swing";
        if (threadName.startsWith("ForkJoinPool"))
            return "ForkJoinPool";
        if (threadName.startsWith("H2-"))
            return "H2 Database";
        if (threadName.startsWith("LogViewer-"))
            return "LogViewer";
        if (threadName.startsWith("ThreadPoolMonitor"))
            return "Monitoring";
        if (threadName.equals("main"))
            return "Main";
        if (threadName.startsWith("Finalizer"))
            return "GC";
        if (threadName.startsWith("Reference Handler"))
            return "GC";
        if (threadName.startsWith("Signal Dispatcher"))
            return "System";

        return "Other";
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    /**
     * Cleanup resources
     */
    public void shutdown() {
        stopMonitoring();
        logger.log("🔚 [THREAD-MONITOR] Monitor shutdown completed");
    }
}