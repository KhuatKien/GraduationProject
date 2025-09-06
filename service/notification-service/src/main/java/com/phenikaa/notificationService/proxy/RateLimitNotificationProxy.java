package com.phenikaa.notificationService.proxy;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy để giới hạn tần suất gửi notification
 * Proxy Pattern: Kiểm soát truy cập và rate limiting
 */
@Slf4j
public class RateLimitNotificationProxy implements NotificationHandlerProxy {

    private final NotificationHandler realHandler;
    private final int maxRequestsPerMinute;
    private final ConcurrentHashMap<Integer, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimitNotificationProxy(NotificationHandler realHandler, int maxRequestsPerMinute) {
        this.realHandler = realHandler;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    public RateLimitNotificationProxy(NotificationHandler realHandler) {
        this(realHandler, 10); // Mặc định 10 requests/phút
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        log.info("RateLimitProxy: Checking rate limit for receiver: {}", receiverId);

        if (!canHandle(senderId, receiverId, title, message, type, actionUrl)) {
            log.warn("RateLimitProxy: Rate limit exceeded for receiver: {}", receiverId);
            return Mono.error(new RuntimeException("Rate limit exceeded. Please try again later."));
        }

        log.info("RateLimitProxy: Rate limit check passed, delegating to real handler");
        return realHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl);
    }

    @Override
    public boolean canHandle(Integer senderId, Integer receiverId, String title, String message,
            NotificationType type, String actionUrl) {
        if (receiverId == null) {
            return false;
        }

        Instant now = Instant.now();
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(receiverId,
                k -> new RateLimitInfo(now, 0));

        // Reset counter nếu đã qua 1 phút
        if (Duration.between(rateLimitInfo.getFirstRequestTime(), now).toMinutes() >= 1) {
            rateLimitInfo.reset(now);
        }

        // Kiểm tra rate limit
        if (rateLimitInfo.getRequestCount() >= maxRequestsPerMinute) {
            log.warn("RateLimitProxy: Rate limit exceeded for receiver {}: {}/{} requests",
                    receiverId, rateLimitInfo.getRequestCount(), maxRequestsPerMinute);
            return false;
        }

        // Tăng counter
        rateLimitInfo.increment();
        log.debug("RateLimitProxy: Rate limit check passed for receiver {}: {}/{} requests",
                receiverId, rateLimitInfo.getRequestCount(), maxRequestsPerMinute);

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

    /**
     * Inner class để lưu thông tin rate limiting
     */
    private static class RateLimitInfo {
        private Instant firstRequestTime;
        private int requestCount;

        public RateLimitInfo(Instant firstRequestTime, int requestCount) {
            this.firstRequestTime = firstRequestTime;
            this.requestCount = requestCount;
        }

        public void reset(Instant now) {
            this.firstRequestTime = now;
            this.requestCount = 0;
        }

        public void increment() {
            this.requestCount++;
        }

        public Instant getFirstRequestTime() {
            return firstRequestTime;
        }

        public int getRequestCount() {
            return requestCount;
        }
    }
}
