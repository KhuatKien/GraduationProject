package com.phenikaa.notificationService.decorator;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Decorator để thêm retry logic cho notification handlers
 * Decorator Pattern: Thêm tính năng retry mà không thay đổi handler gốc
 */
@Slf4j
public class RetryNotificationDecorator extends NotificationHandlerDecorator {

    private final int maxRetries;
    private final Duration retryDelay;

    public RetryNotificationDecorator(NotificationHandler wrappedHandler, int maxRetries, Duration retryDelay) {
        super(wrappedHandler);
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    public RetryNotificationDecorator(NotificationHandler wrappedHandler) {
        this(wrappedHandler, 3, Duration.ofSeconds(2));
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        return retryWithBackoff(senderId, receiverId, title, message, type, actionUrl, 0);
    }

    private Mono<Notification> retryWithBackoff(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl, int attempt) {
        return wrappedHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                .doOnSuccess(notification -> {
                    if (attempt > 0) {
                        log.info("Notification succeeded on attempt {} after {} retries", attempt + 1, attempt);
                    }
                })
                .onErrorResume(error -> {
                    if (attempt < maxRetries) {
                        log.warn("Notification failed on attempt {}: {}. Retrying in {}ms...",
                                attempt + 1, error.getMessage(), retryDelay.toMillis());
                        return Mono.delay(retryDelay)
                                .then(retryWithBackoff(senderId, receiverId, title, message, type, actionUrl,
                                        attempt + 1));
                    } else {
                        log.error("Notification failed after {} attempts. Giving up.", maxRetries + 1);
                        return Mono.error(error);
                    }
                });
    }
}
