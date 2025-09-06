package com.phenikaa.notificationService.proxy;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Proxy để kiểm tra quyền và bảo mật trước khi xử lý notification
 * Proxy Pattern: Kiểm soát truy cập và bảo mật
 */
@Slf4j
public class SecurityNotificationProxy implements NotificationHandlerProxy {

    private final NotificationHandler realHandler;
    private final Set<Integer> blockedUsers;
    private final Set<NotificationType> restrictedTypes;

    public SecurityNotificationProxy(NotificationHandler realHandler,
            Set<Integer> blockedUsers,
            Set<NotificationType> restrictedTypes) {
        this.realHandler = realHandler;
        this.blockedUsers = blockedUsers;
        this.restrictedTypes = restrictedTypes;
    }

    public SecurityNotificationProxy(NotificationHandler realHandler) {
        this(realHandler, Set.of(), Set.of()); // Mặc định không block ai
    }

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId, String title,
            String message, NotificationType type, String actionUrl) {
        log.info("SecurityProxy: Checking security for receiver: {}", receiverId);

        if (!canHandle(senderId, receiverId, title, message, type, actionUrl)) {
            log.warn("SecurityProxy: Security check failed for receiver: {}", receiverId);
            return Mono.error(new SecurityException("Access denied. Security check failed."));
        }

        log.info("SecurityProxy: Security check passed, delegating to real handler");
        return realHandler.handleNotification(senderId, receiverId, title, message, type, actionUrl);
    }

    @Override
    public boolean canHandle(Integer senderId, Integer receiverId, String title, String message,
            NotificationType type, String actionUrl) {
        if (receiverId == null) {
            return false;
        }

        // Kiểm tra user bị block
        if (blockedUsers.contains(receiverId)) {
            log.warn("SecurityProxy: Receiver {} is blocked", receiverId);
            return false;
        }

        // Kiểm tra sender bị block (nếu có)
        if (senderId != null && blockedUsers.contains(senderId)) {
            log.warn("SecurityProxy: Sender {} is blocked", senderId);
            return false;
        }

        // Kiểm tra loại notification bị hạn chế
        if (restrictedTypes.contains(type)) {
            log.warn("SecurityProxy: Notification type {} is restricted", type);
            return false;
        }

        // Kiểm tra nội dung có chứa từ khóa nhạy cảm không
        if (containsSensitiveContent(title, message)) {
            log.warn("SecurityProxy: Sensitive content detected in title or message");
            return false;
        }

        log.debug("SecurityProxy: All security checks passed");
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
     * Kiểm tra nội dung có chứa từ khóa nhạy cảm không
     */
    private boolean containsSensitiveContent(String title, String message) {
        if (title == null || message == null) {
            return false;
        }

        String content = (title + " " + message).toLowerCase();
        String[] sensitiveWords = { "spam", "scam", "hack", "virus", "malware" };

        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                log.warn("SecurityProxy: Sensitive word detected: {}", word);
                return true;
            }
        }

        return false;
    }
}
