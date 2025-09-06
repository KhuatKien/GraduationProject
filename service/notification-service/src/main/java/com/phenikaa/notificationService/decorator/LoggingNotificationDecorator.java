package com.phenikaa.notificationService.decorator;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Decorator để thêm logging chi tiết cho notification handlers
 * Decorator Pattern: Thêm tính năng logging mà không thay đổi handler gốc
 */
@Slf4j
public class LoggingNotificationDecorator extends NotificationHandlerDecorator {

    public LoggingNotificationDecorator(NotificationHandler wrappedHandler) {
        super(wrappedHandler);
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        log.info("=== NOTIFICATION PROCESSING START ===");
        log.info("Handler: {} | Channel: {}", wrappedHandler.getClass().getSimpleName(), getChannel());
        log.info("Sender: {} | Receiver: {} | Type: {}", senderId, receiverId, type);
        log.info("Title: {} | Message: {}", title, message);
        log.info("Action URL: {}", actionUrl);

        long startTime = System.currentTimeMillis();

        return wrappedHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                .doOnSuccess(notification -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Notification processed successfully in {}ms", duration);
                    log.info("Notification ID: {} | Status: SUCCESS", notification.getNotificationId());
                    log.info("=== NOTIFICATION PROCESSING END ===");
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Notification processing failed in {}ms", duration);
                    log.error("Error: {} | Status: FAILED", error.getMessage());
                    log.info("=== NOTIFICATION PROCESSING END ===");
                });
    }
}
