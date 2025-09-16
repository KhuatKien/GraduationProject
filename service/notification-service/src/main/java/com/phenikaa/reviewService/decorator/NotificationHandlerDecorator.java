package com.phenikaa.reviewService.decorator;

import com.phenikaa.reviewService.entity.Notification;
import com.phenikaa.reviewService.entity.NotificationChannel;
import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.handler.NotificationHandler;
import reactor.core.publisher.Mono;

/**
 * Abstract Decorator cho NotificationHandler
 * Decorator Pattern: Thêm tính năng bổ sung vào handlers mà không thay đổi cấu
 * trúc cơ bản
 */
public abstract class NotificationHandlerDecorator implements NotificationHandler {

    protected final NotificationHandler wrappedHandler;

    public NotificationHandlerDecorator(NotificationHandler wrappedHandler) {
        this.wrappedHandler = wrappedHandler;
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        return wrappedHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl);
    }

    @Override
    public NotificationChannel getChannel() {
        return wrappedHandler.getChannel();
    }

    @Override
    public boolean supports(NotificationType type) {
        return wrappedHandler.supports(type);
    }
}
