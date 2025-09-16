package com.phenikaa.reviewService.service;

import com.phenikaa.reviewService.entity.Notification;
import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.handler.NotificationHandler;
import com.phenikaa.reviewService.factory.NotificationFactoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Service xử lý notification bất đồng bộ với CompletableFuture
 * Sử dụng ExecutorService để xử lý song song
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationService {

    private final NotificationFactoryProvider factoryProvider;

    @Qualifier("notificationExecutor")
    private final Executor notificationExecutor;

    @Qualifier("emailExecutor")
    private final Executor emailExecutor;

    /**
     * Gửi notification bất đồng bộ
     * 
     * @param senderId   ID người gửi
     * @param receiverId ID người nhận
     * @param title      Tiêu đề
     * @param message    Nội dung
     * @param type       Loại notification
     * @param actionUrl  URL hành động
     * @return CompletableFuture<Notification>
     */
    public CompletableFuture<Notification> sendNotificationAsync(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl) {
        log.info("Starting async notification processing for receiver: {}", receiverId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                NotificationHandler handler = factoryProvider.getHandlerByType(type);
                log.info("Async processing notification with handler: {}", handler.getClass().getSimpleName());

                return handler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                        .block(); // Block để chuyển từ Mono sang Notification
            } catch (Exception e) {
                log.error("Async notification processing failed for receiver {}: {}", receiverId, e.getMessage());
                throw new RuntimeException("Async notification processing failed", e);
            }
        }, notificationExecutor)
                .whenComplete((notification, throwable) -> {
                    if (throwable != null) {
                        log.error("Async notification completed with error for receiver {}: {}", receiverId,
                                throwable.getMessage());
                    } else {
                        log.info("Async notification completed successfully for receiver {}: {}", receiverId,
                                notification.getNotificationId());
                    }
                });
    }

    /**
     * Gửi notification bất đồng bộ với callback
     * 
     * @param senderId   ID người gửi
     * @param receiverId ID người nhận
     * @param title      Tiêu đề
     * @param message    Nội dung
     * @param type       Loại notification
     * @param actionUrl  URL hành động
     * @param onSuccess  Callback khi thành công
     * @param onError    Callback khi thất bại
     */
    public void sendNotificationWithCallback(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl,
            java.util.function.Consumer<Notification> onSuccess,
            java.util.function.Consumer<Throwable> onError) {

        sendNotificationAsync(senderId, receiverId, title, message, type, actionUrl)
                .thenAccept(notification -> {
                    log.info("Notification callback - Success: {}", notification.getNotificationId());
                    onSuccess.accept(notification);
                })
                .exceptionally(throwable -> {
                    log.error("Notification callback - Error: {}", throwable.getMessage());
                    onError.accept(throwable);
                    return null;
                });
    }

    /**
     * Gửi nhiều notification song song
     * 
     * @param requests Danh sách notification requests
     * @return CompletableFuture<List<Notification>>
     */
    public CompletableFuture<List<Notification>> sendMultipleNotificationsAsync(
            List<NotificationRequest> requests) {

        log.info("Starting async batch processing for {} notifications", requests.size());

        List<CompletableFuture<Notification>> futures = requests.stream()
                .map(req -> sendNotificationAsync(
                        req.getSenderId(),
                        req.getReceiverId(),
                        req.getTitle(),
                        req.getMessage(),
                        req.getType(),
                        req.getActionUrl()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .whenComplete((notifications, throwable) -> {
                    if (throwable != null) {
                        log.error("Batch processing completed with error: {}", throwable.getMessage());
                    } else {
                        log.info("Batch processing completed successfully: {} notifications", notifications.size());
                    }
                });
    }

    /**
     * Gửi notification với timeout
     * 
     * @param senderId       ID người gửi
     * @param receiverId     ID người nhận
     * @param title          Tiêu đề
     * @param message        Nội dung
     * @param type           Loại notification
     * @param actionUrl      URL hành động
     * @param timeoutSeconds Timeout trong giây
     * @return CompletableFuture<Notification>
     */
    public CompletableFuture<Notification> sendNotificationWithTimeout(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl,
            long timeoutSeconds) {

        log.info("Starting async notification with timeout {}s for receiver: {}", timeoutSeconds, receiverId);

        return sendNotificationAsync(senderId, receiverId, title, message, type, actionUrl)
                .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .whenComplete((notification, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            log.warn("Notification timeout after {}s for receiver: {}", timeoutSeconds, receiverId);
                        } else {
                            log.error("Notification failed for receiver {}: {}", receiverId, throwable.getMessage());
                        }
                    } else {
                        log.info("Notification completed within timeout for receiver: {}", receiverId);
                    }
                });
    }

    /**
     * Inner class cho notification request
     */
    public static class NotificationRequest {
        private Integer senderId;
        private Integer receiverId;
        private String title;
        private String message;
        private NotificationType type;
        private String actionUrl;

        // Constructors, getters, setters
        public NotificationRequest() {
        }

        public NotificationRequest(Integer senderId, Integer receiverId, String title,
                String message, NotificationType type, String actionUrl) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.actionUrl = actionUrl;
        }

        // Getters and setters
        public Integer getSenderId() {
            return senderId;
        }

        public void setSenderId(Integer senderId) {
            this.senderId = senderId;
        }

        public Integer getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Integer receiverId) {
            this.receiverId = receiverId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public NotificationType getType() {
            return type;
        }

        public void setType(NotificationType type) {
            this.type = type;
        }

        public String getActionUrl() {
            return actionUrl;
        }

        public void setActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
        }
    }
}
