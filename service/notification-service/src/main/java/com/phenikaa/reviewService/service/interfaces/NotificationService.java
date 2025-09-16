package com.phenikaa.reviewService.service.interfaces;

import com.phenikaa.reviewService.entity.Notification;
import com.phenikaa.reviewService.entity.NotificationChannel;
import com.phenikaa.reviewService.entity.NotificationType;
import reactor.core.publisher.Mono;

public interface NotificationService {
        Mono<Notification> createNotification(Integer senderId, Integer receiverId, String title, String message,
                        NotificationType type, String actionUrl);

        Mono<Long> markAllAsReadAndPublish(Integer receiverId);

        Mono<Notification> toggleReadAndPublish(Integer receiverId, String notificationId);

        /**
         * Tạo notification với channel cụ thể
         */
        Mono<Notification> createNotificationWithChannel(Integer senderId, Integer receiverId,
                        String title, String message,
                        NotificationType type, String actionUrl,
                        NotificationChannel channel);

        /**
         * Gửi thông báo đến tất cả user
         */
        void broadcastToAllUsers(Integer senderId, String title, String message,
                        NotificationType type, String actionUrl);
}
