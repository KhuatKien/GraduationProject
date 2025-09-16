package com.phenikaa.reviewService.handler;

import com.phenikaa.reviewService.entity.Notification;
import com.phenikaa.reviewService.entity.NotificationChannel;
import com.phenikaa.reviewService.entity.NotificationType;
import reactor.core.publisher.Mono;

public interface NotificationHandler {
    /**
     * Xử lý tạo notification
     */
    Mono<Notification> handleNotification(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl);

    /**
     * Lấy loại channel mà handler này hỗ trợ
     */
    NotificationChannel getChannel();

    /**
     * Kiểm tra xem handler có hỗ trợ notification type này không
     */
    boolean supports(NotificationType type);
}
