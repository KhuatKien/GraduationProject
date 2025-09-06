package com.phenikaa.notificationService.service;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import com.phenikaa.notificationService.factory.NotificationFactoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service xử lý batch notification với CompletableFuture
 * Sử dụng ExecutorService riêng cho batch processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchNotificationProcessor {

    private final NotificationFactoryProvider factoryProvider;

    @Qualifier("batchExecutor")
    private final Executor batchExecutor;

    /**
     * Xử lý batch notification theo loại
     * 
     * @param requests Danh sách notification requests
     * @return CompletableFuture<List<Notification>>
     */
    public CompletableFuture<List<Notification>> processBatchByType(
            List<AsyncNotificationService.NotificationRequest> requests) {

        log.info("Starting batch processing by type for {} notifications", requests.size());

        // Nhóm requests theo loại notification
        var groupedRequests = requests.stream()
                .collect(Collectors.groupingBy(AsyncNotificationService.NotificationRequest::getType));

        log.info("Grouped into {} notification types", groupedRequests.size());

        // Xử lý song song từng nhóm
        List<CompletableFuture<List<Notification>>> groupFutures = groupedRequests.entrySet().stream()
                .map(entry -> processGroupAsync(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Kết hợp tất cả kết quả
        return CompletableFuture.allOf(groupFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> groupFutures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .whenComplete((notifications, throwable) -> {
                    if (throwable != null) {
                        log.error("Batch processing by type completed with error: {}", throwable.getMessage());
                    } else {
                        log.info("Batch processing by type completed successfully: {} notifications",
                                notifications.size());
                    }
                });
    }

    /**
     * Xử lý một nhóm notification cùng loại
     */
    private CompletableFuture<List<Notification>> processGroupAsync(
            NotificationType type,
            List<AsyncNotificationService.NotificationRequest> requests) {

        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing group of {} notifications for type: {}", requests.size(), type);

            try {
                NotificationHandler handler = factoryProvider.getHandlerByType(type);
                log.info("Using handler: {} for type: {}", handler.getClass().getSimpleName(), type);

                // Xử lý song song từng notification trong nhóm
                List<CompletableFuture<Notification>> futures = requests.stream()
                        .map(req -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return handler.handleNotification(
                                        req.getSenderId(),
                                        req.getReceiverId(),
                                        req.getTitle(),
                                        req.getMessage(),
                                        req.getType(),
                                        req.getActionUrl()).block();
                            } catch (Exception e) {
                                log.error("Failed to process notification for receiver {}: {}",
                                        req.getReceiverId(), e.getMessage());
                                throw new RuntimeException("Notification processing failed", e);
                            }
                        }, batchExecutor))
                        .collect(Collectors.toList());

                // Chờ tất cả hoàn thành
                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.error("Failed to process group for type {}: {}", type, e.getMessage());
                throw new RuntimeException("Group processing failed", e);
            }
        }, batchExecutor);
    }

    /**
     * Xử lý batch notification với progress tracking
     * 
     * @param requests         Danh sách notification requests
     * @param progressCallback Callback để track progress
     * @return CompletableFuture<List<Notification>>
     */
    public CompletableFuture<List<Notification>> processBatchWithProgress(
            List<AsyncNotificationService.NotificationRequest> requests,
            java.util.function.Consumer<BatchProgress> progressCallback) {

        log.info("Starting batch processing with progress tracking for {} notifications", requests.size());

        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Xử lý song song tất cả requests
        List<CompletableFuture<Notification>> futures = requests.stream()
                .map(req -> processSingleWithProgress(req, completedCount, successCount, errorCount, progressCallback))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Notification> results = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());

                    // Final progress update
                    progressCallback.accept(new BatchProgress(
                            requests.size(),
                            completedCount.get(),
                            successCount.get(),
                            errorCount.get()));

                    return results;
                });
    }

    /**
     * Xử lý một notification với progress tracking
     */
    private CompletableFuture<Notification> processSingleWithProgress(
            AsyncNotificationService.NotificationRequest req,
            AtomicInteger completedCount,
            AtomicInteger successCount,
            AtomicInteger errorCount,
            java.util.function.Consumer<BatchProgress> progressCallback) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                NotificationHandler handler = factoryProvider.getHandlerByType(req.getType());
                Notification notification = handler.handleNotification(
                        req.getSenderId(),
                        req.getReceiverId(),
                        req.getTitle(),
                        req.getMessage(),
                        req.getType(),
                        req.getActionUrl()).block();

                successCount.incrementAndGet();
                return notification;

            } catch (Exception e) {
                log.error("Failed to process notification for receiver {}: {}",
                        req.getReceiverId(), e.getMessage());
                errorCount.incrementAndGet();
                throw new RuntimeException("Notification processing failed", e);
            }
        }, batchExecutor)
                .whenComplete((notification, throwable) -> {
                    completedCount.incrementAndGet();

                    // Update progress
                    progressCallback.accept(new BatchProgress(
                            completedCount.get(),
                            completedCount.get(),
                            successCount.get(),
                            errorCount.get()));
                });
    }

    /**
     * Inner class cho batch progress
     */
    public static class BatchProgress {
        private final int total;
        private final int completed;
        private final int success;
        private final int errors;
        private final double percentage;

        public BatchProgress(int total, int completed, int success, int errors) {
            this.total = total;
            this.completed = completed;
            this.success = success;
            this.errors = errors;
            this.percentage = total > 0 ? (double) completed / total * 100 : 0;
        }

        // Getters
        public int getTotal() {
            return total;
        }

        public int getCompleted() {
            return completed;
        }

        public int getSuccess() {
            return success;
        }

        public int getErrors() {
            return errors;
        }

        public double getPercentage() {
            return percentage;
        }

        @Override
        public String toString() {
            return String.format("BatchProgress{total=%d, completed=%d, success=%d, errors=%d, percentage=%.1f%%}",
                    total, completed, success, errors, percentage);
        }
    }
}
