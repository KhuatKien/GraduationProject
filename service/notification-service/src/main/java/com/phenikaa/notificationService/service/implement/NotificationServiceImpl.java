package com.phenikaa.notificationService.service.implement;

import com.phenikaa.notificationService.broadcaster.NotificationBroadcaster;
import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.factory.NotificationFactoryProvider;
import com.phenikaa.notificationService.handler.NotificationHandler;
import com.phenikaa.notificationService.repository.NotificationRepository;
import com.phenikaa.notificationService.service.interfaces.NotificationService;
import com.phenikaa.notificationService.service.AsyncNotificationService;
import com.phenikaa.notificationService.service.BatchNotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    // private final NotificationPublisher notificationBroadcaster;
    private final ReactiveMongoTemplate mongoTemplate;
    private final NotificationBroadcaster notificationBroadcaster;
    private final NotificationFactoryProvider factoryProvider;
    private final AsyncNotificationService asyncNotificationService;
    private final BatchNotificationProcessor batchProcessor;

    @Override
    public Mono<Notification> createNotification(Integer senderId, Integer receiverId, String title, String message,
            NotificationType type, String actionUrl) {
        // Sử dụng Factory Method để tự động chọn handler phù hợp
        NotificationHandler handler = factoryProvider.getHandlerByType(type);

        return handler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                .flatMap(notification -> notificationRepository.save(notification))
                .doOnSuccess(saved -> notificationBroadcaster.publish(receiverId, saved));
    }

    @Override
    public Mono<Long> markAllAsReadAndPublish(Integer receiverId) {
        Query query = new Query(
                Criteria.where("receiverId").is(receiverId)
                        .and("read").is(false));

        return mongoTemplate.find(query, Notification.class)
                .collectList()
                .flatMap(notifications -> {
                    if (notifications.isEmpty()) {
                        return Mono.just(0L);
                    }

                    Update update = new Update().set("read", true);
                    return mongoTemplate.updateMulti(query, update, Notification.class)
                            .map(result -> {
                                notifications.forEach(notice -> {
                                    notice.setRead(true); // update lại local
                                    notificationBroadcaster.publish(receiverId, notice);
                                });
                                return (long) result.getModifiedCount();
                            });
                });
    }

    @Override
    public Mono<Notification> toggleReadAndPublish(Integer receiverId, String notificationId) {
        Query query = new Query(Criteria.where("receiverId").is(receiverId)
                .and("_id").is(notificationId));

        return mongoTemplate.findOne(query, Notification.class)
                .flatMap(existing -> {
                    boolean newValue = !existing.isRead();
                    Update update = new Update().set("read", newValue);

                    return mongoTemplate.findAndModify(query, update, Notification.class)
                            .doOnSuccess(updatedNotice -> {
                                if (updatedNotice != null) {
                                    notificationBroadcaster.publish(receiverId, updatedNotice);
                                }
                            });
                });
    }

    /**
     * Tạo notification với channel cụ thể
     */
    @Override
    public Mono<Notification> createNotificationWithChannel(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl,
            NotificationChannel channel) {
        // Sử dụng Abstract Factory để tạo handler
        NotificationHandler handler = factoryProvider.getHandler(channel);

        return handler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                .flatMap(notification -> notificationRepository.save(notification))
                .doOnSuccess(saved -> notificationBroadcaster.publish(receiverId, saved));
    }

    /**
     * Tạo notification bất đồng bộ
     */
    public java.util.concurrent.CompletableFuture<Notification> createNotificationAsync(Integer senderId,
            Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl) {
        log.info("Creating async notification for receiver: {}", receiverId);

        return asyncNotificationService.sendNotificationAsync(senderId, receiverId, title, message, type, actionUrl)
                .thenApply(notification -> {
                    // Lưu vào database
                    notificationRepository.save(notification).block();
                    // Broadcast
                    notificationBroadcaster.publish(receiverId, notification);
                    log.info("Async notification created and broadcasted: {}", notification.getNotificationId());
                    return notification;
                })
                .exceptionally(throwable -> {
                    log.error("Async notification creation failed for receiver {}: {}", receiverId,
                            throwable.getMessage());
                    throw new RuntimeException("Async notification creation failed", throwable);
                });
    }

    /**
     * Tạo nhiều notification bất đồng bộ
     */
    public java.util.concurrent.CompletableFuture<java.util.List<Notification>> createMultipleNotificationsAsync(
            java.util.List<AsyncNotificationService.NotificationRequest> requests) {

        log.info("Creating {} async notifications", requests.size());

        return asyncNotificationService.sendMultipleNotificationsAsync(requests)
                .thenApply(notifications -> {
                    // Lưu tất cả vào database
                    notifications.forEach(notification -> {
                        notificationRepository.save(notification).block();
                        notificationBroadcaster.publish(notification.getReceiverId(), notification);
                    });
                    log.info("All {} async notifications created and broadcasted", notifications.size());
                    return notifications;
                })
                .exceptionally(throwable -> {
                    log.error("Batch async notification creation failed: {}", throwable.getMessage());
                    throw new RuntimeException("Batch async notification creation failed", throwable);
                });
    }

    /**
     * Xử lý batch notification với progress tracking
     */
    public java.util.concurrent.CompletableFuture<java.util.List<Notification>> processBatchWithProgress(
            java.util.List<AsyncNotificationService.NotificationRequest> requests,
            java.util.function.Consumer<BatchNotificationProcessor.BatchProgress> progressCallback) {

        log.info("Processing batch with progress tracking for {} notifications", requests.size());

        return batchProcessor.processBatchWithProgress(requests, progressCallback)
                .thenApply(notifications -> {
                    // Lưu tất cả vào database
                    notifications.forEach(notification -> {
                        notificationRepository.save(notification).block();
                        notificationBroadcaster.publish(notification.getReceiverId(), notification);
                    });
                    log.info("Batch processing completed: {} notifications saved and broadcasted",
                            notifications.size());
                    return notifications;
                })
                .exceptionally(throwable -> {
                    log.error("Batch processing with progress failed: {}", throwable.getMessage());
                    throw new RuntimeException("Batch processing with progress failed", throwable);
                });
    }

    @Override
    public void broadcastToAllUsers(Integer senderId, String title, String message,
            NotificationType type, String actionUrl) {
        log.info("Broadcasting notification to all users: {}", title);

        try {
            // Lấy danh sách tất cả user từ user-service
            // Tạm thời sử dụng danh sách user cố định, sau này có thể gọi API user-service
            // Để đơn giản, tôi sẽ tạo một số user ID mẫu
            // Trong thực tế, cần gọi API user-service để lấy danh sách user thật

            // TODO: Gọi API user-service để lấy danh sách tất cả user
            // Tạm thời sử dụng danh sách user mẫu
            java.util.List<Integer> allUserIds = java.util.Arrays.asList(1, 2, 3, 4, 5);

            // Tạo notification cho từng user
            for (Integer userId : allUserIds) {
                createNotification(senderId, userId, title, message, type, actionUrl)
                        .subscribe(
                                notification -> log.info("Broadcast notification sent to user {}: {}", userId,
                                        notification.getNotificationId()),
                                error -> log.error("Failed to send broadcast notification to user {}: {}", userId,
                                        error.getMessage()));
            }

            log.info("Broadcast notification initiated for {} users", allUserIds.size());

        } catch (Exception e) {
            log.error("Error broadcasting notification to all users: {}", e.getMessage());
            throw new RuntimeException("Failed to broadcast notification", e);
        }
    }

}
