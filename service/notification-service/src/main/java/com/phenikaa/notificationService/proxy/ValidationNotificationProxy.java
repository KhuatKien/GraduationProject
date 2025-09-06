package com.phenikaa.notificationService.proxy;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Proxy để validate input trước khi xử lý notification
 * Proxy Pattern: Kiểm soát truy cập và validate dữ liệu đầu vào
 */
@Slf4j
public class ValidationNotificationProxy implements NotificationHandlerProxy {

    private final NotificationHandler realHandler;

    public ValidationNotificationProxy(NotificationHandler realHandler) {
        this.realHandler = realHandler;
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        log.info("ValidationProxy: Validating notification request");

        // Validate input
        if (!canHandle(senderId, receiverId, title, message, type, actionUrl)) {
            log.error("ValidationProxy: Invalid notification request");
            return Mono.error(new IllegalArgumentException("Invalid notification request"));
        }

        log.info("ValidationProxy: Request validated successfully, delegating to real handler");
        return realHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl);
    }

    @Override
    public boolean canHandle(Integer senderId, Integer receiverId, String title, String message,
            NotificationType type, String actionUrl) {
        // Validate receiverId
        if (receiverId == null || receiverId <= 0) {
            log.warn("ValidationProxy: Invalid receiverId: {}", receiverId);
            return false;
        }

        // Validate title
        if (title == null || title.trim().isEmpty()) {
            log.warn("ValidationProxy: Invalid title: {}", title);
            return false;
        }

        // Validate message
        if (message == null || message.trim().isEmpty()) {
            log.warn("ValidationProxy: Invalid message: {}", message);
            return false;
        }

        // Validate type
        if (type == null) {
            log.warn("ValidationProxy: Invalid type: {}", type);
            return false;
        }

        // Validate title length
        if (title.length() > 200) {
            log.warn("ValidationProxy: Title too long: {} characters", title.length());
            return false;
        }

        // Validate message length
        if (message.length() > 1000) {
            log.warn("ValidationProxy: Message too long: {} characters", message.length());
            return false;
        }

        log.debug("ValidationProxy: All validations passed");
        return true;
    }

    @Override
    public NotificationChannel getChannel() {
        return realHandler.getChannel();
    }

    @Override
    public boolean supports(NotificationType type) {
        return realHandler.supports(type);
    }

    @Override
    public NotificationHandler getRealHandler() {
        return realHandler;
    }
}
