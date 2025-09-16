package com.phenikaa.reviewService.decorator;

import com.phenikaa.reviewService.entity.Notification;
import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator để thêm caching cho notification handlers
 * Decorator Pattern: Thêm tính năng caching mà không thay đổi handler gốc
 */
@Slf4j
public class CachingNotificationDecorator extends NotificationHandlerDecorator {

    private final ConcurrentHashMap<String, Notification> cache = new ConcurrentHashMap<>();
    private final Duration cacheExpiry;

    public CachingNotificationDecorator(NotificationHandler wrappedHandler, Duration cacheExpiry) {
        super(wrappedHandler);
        this.cacheExpiry = cacheExpiry;
    }

    public CachingNotificationDecorator(NotificationHandler wrappedHandler) {
        this(wrappedHandler, Duration.ofMinutes(5));
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        String cacheKey = generateCacheKey(senderId, receiverId, title, message, type);

        // Kiểm tra cache trước
        Notification cachedNotification = cache.get(cacheKey);
        if (cachedNotification != null) {
            log.info("Returning cached notification for key: {}", cacheKey);
            return Mono.just(cachedNotification);
        }

        // Nếu không có trong cache, gọi handler gốc
        log.info("Cache miss for key: {}. Calling wrapped handler.", cacheKey);
        return wrappedHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl)
                .doOnSuccess(notification -> {
                    // Lưu vào cache
                    cache.put(cacheKey, notification);
                    log.info("Notification cached with key: {}", cacheKey);

                    // Xóa cache sau thời gian expiry (đơn giản)
                    Mono.delay(cacheExpiry)
                            .subscribe(unused -> {
                                cache.remove(cacheKey);
                                log.debug("Cache expired and removed for key: {}", cacheKey);
                            });
                });
    }

    private String generateCacheKey(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type) {
        return String.format("%s_%s_%s_%s_%s",
                senderId, receiverId, title.hashCode(), message.hashCode(), type);
    }
}
