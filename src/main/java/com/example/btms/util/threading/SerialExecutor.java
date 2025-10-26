package com.example.btms.util.threading;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * SerialExecutor đảm bảo các task được thực thi TUẦN TỰ theo FIFO cho một "ngữ
 * cảnh" (ví dụ: mỗi sân),
 * nhưng sử dụng một backing executor dùng chung (khuyên dùng virtual threads
 * Java 21).
 *
 * - Dùng 1 instance SerialExecutor cho mỗi sân để tránh race condition trên
 * state của sân đó.
 * - Backing executor có thể là shared virtualThreadExecutor hoặc IO pool.
 */
public class SerialExecutor implements Executor {

    private final Executor executor; // backing
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private Runnable active;
    private final String name;

    public SerialExecutor(Executor executor, String name) {
        this.executor = Objects.requireNonNull(executor, "backing executor");
        this.name = (name == null ? "serial" : name);
    }

    /**
     * Gửi một task không trả về, trả CompletableFuture để caller theo dõi hoàn
     * tất/lỗi.
     */
    public CompletableFuture<Void> submit(Runnable task) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        execute(() -> {
            try {
                task.run();
                cf.complete(null);
            } catch (Throwable t) {
                cf.completeExceptionally(t);
            }
        });
        return cf;
    }

    /**
     * Thực thi tuần tự: bọc task để tự gọi scheduleNext() sau khi xong.
     */
    @Override
    public synchronized void execute(Runnable command) {
        tasks.offer(() -> {
            try {
                command.run();
            } finally {
                scheduleNext();
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    private synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }

    public synchronized int getQueueSize() {
        return tasks.size();
    }

    public String getName() {
        return name;
    }

    /**
     * Xoá hàng đợi còn lại (không huỷ task đang chạy).
     */
    public synchronized void clearQueue() {
        tasks.clear();
    }
}
