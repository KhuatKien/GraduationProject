package com.phenikaa.notificationService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Cấu hình cho Async processing với ExecutorService
 * Sử dụng ThreadPoolExecutor để xử lý notification song song
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * ExecutorService cho notification processing
     * ThreadPoolExecutor với core pool size 5, max pool size 20
     */
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, // Core pool size
                20, // Maximum pool size
                60L, TimeUnit.SECONDS, // Keep alive time
                new LinkedBlockingQueue<>(100), // Queue capacity
                r -> {
                    Thread thread = new Thread(r, "notification-thread-" + System.currentTimeMillis());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        log.info("Notification ExecutorService configured: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().remainingCapacity());

        return executor;
    }

    /**
     * ExecutorService cho batch processing
     * ThreadPoolExecutor riêng cho xử lý hàng loạt
     */
    @Bean("batchExecutor")
    public Executor batchExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, // Core pool size
                10, // Maximum pool size
                30L, TimeUnit.SECONDS, // Keep alive time
                new LinkedBlockingQueue<>(50), // Queue capacity
                r -> {
                    Thread thread = new Thread(r, "batch-thread-" + System.currentTimeMillis());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        log.info("Batch ExecutorService configured: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().remainingCapacity());

        return executor;
    }

    /**
     * ExecutorService cho email processing
     * ThreadPoolExecutor riêng cho email (có thể chậm)
     */
    @Bean("emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, // Core pool size
                15, // Maximum pool size
                120L, TimeUnit.SECONDS, // Keep alive time
                new LinkedBlockingQueue<>(200), // Queue capacity
                r -> {
                    Thread thread = new Thread(r, "email-thread-" + System.currentTimeMillis());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        log.info("Email ExecutorService configured: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().remainingCapacity());

        return executor;
    }
}
