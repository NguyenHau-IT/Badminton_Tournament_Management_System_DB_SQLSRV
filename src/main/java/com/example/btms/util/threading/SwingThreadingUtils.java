package com.example.btms.util.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🎨 Enhanced Swing Threading Utilities cho Java 21
 * 
 * Cải thiện hiệu năng UI và tránh blocking EDT (Event Dispatch Thread)
 */
public final class SwingThreadingUtils {

    private static final Logger log = LoggerFactory.getLogger(SwingThreadingUtils.class);

    // Thread pool để thực thi background tasks không liên quan đến UI
    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "BTMS-Swing-Background-" + System.currentTimeMillis());
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    });

    private SwingThreadingUtils() {
        // Utility class
    }

    /**
     * 🚀 Thực thi task trên EDT một cách an toàn
     */
    public static void runOnEDT(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            // Đã ở trên EDT, chạy trực tiếp
            try {
                task.run();
            } catch (Exception e) {
                log.warn("EDT task execution error: {}", e.getMessage());
            }
        } else {
            // Chuyển lên EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.warn("EDT invokeLater task error: {}", e.getMessage());
                }
            });
        }
    }

    /**
     * 🔄 Thực thi background task, sau đó update UI trên EDT
     */
    public static <T> CompletableFuture<T> executeBackgroundThenUpdateUI(
            Supplier<T> backgroundTask,
            java.util.function.Consumer<T> uiUpdateTask) {

        return CompletableFuture
                .supplyAsync(backgroundTask, BACKGROUND_EXECUTOR)
                .whenComplete((result, throwable) -> {
                    runOnEDT(() -> {
                        if (throwable != null) {
                            log.warn("Background task failed: {}", throwable.getMessage());
                            // Có thể show error dialog hoặc handle error
                        } else if (uiUpdateTask != null) {
                            try {
                                uiUpdateTask.accept(result);
                            } catch (Exception e) {
                                log.warn("UI update task error: {}", e.getMessage());
                            }
                        }
                    });
                });
    }

    /**
     * 💾 Thực thi I/O operation trên background thread
     */
    public static CompletableFuture<Void> executeIOTask(Runnable ioTask) {
        return CompletableFuture.runAsync(() -> {
            try {
                ioTask.run();
            } catch (Exception e) {
                log.warn("I/O task error: {}", e.getMessage());
            }
        }, BACKGROUND_EXECUTOR);
    }

    /**
     * 🎯 Thực thi task với timeout để tránh freeze UI
     */
    public static <T> CompletableFuture<T> executeWithTimeout(
            Supplier<T> task,
            long timeoutSeconds) {

        return CompletableFuture
                .supplyAsync(task, BACKGROUND_EXECUTOR)
                .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        log.warn("Task timed out after {} seconds", timeoutSeconds);
                    } else {
                        log.warn("Task execution error: {}", throwable.getMessage());
                    }
                    return null;
                });
    }

    /**
     * 📊 Utility method để check EDT performance
     */
    public static void logEDTStatus() {
        if (SwingUtilities.isEventDispatchThread()) {
            log.debug("Currently running on EDT");
        } else {
            log.debug("Currently running on background thread: {}",
                    Thread.currentThread().getName());
        }
    }

    /**
     * 🧹 Cleanup resources
     */
    public static void shutdown() {
        if (!BACKGROUND_EXECUTOR.isShutdown()) {
            BACKGROUND_EXECUTOR.shutdown();
            log.info("SwingThreadingUtils background executor shut down");
        }
    }

    /**
     * 🎨 Enhanced version của invokeLater với error handling
     */
    public static void safeInvokeLater(Runnable task, String taskDescription) {
        SwingUtilities.invokeLater(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.warn("EDT task '{}' failed: {}", taskDescription, e.getMessage());
            }
        });
    }

    /**
     * ⚡ Thực thi nhanh trên EDT với fallback cho background thread nếu cần
     */
    public static void runSmartOnEDT(Runnable task, boolean canRunOnBackground) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                task.run();
            } catch (Exception e) {
                log.warn("EDT task execution error: {}", e.getMessage());
            }
        } else if (canRunOnBackground) {
            // Có thể chạy ngay trên background thread
            try {
                task.run();
            } catch (Exception e) {
                log.warn("Background task execution error: {}", e.getMessage());
            }
        } else {
            // Phải chạy trên EDT
            runOnEDT(task);
        }
    }
}