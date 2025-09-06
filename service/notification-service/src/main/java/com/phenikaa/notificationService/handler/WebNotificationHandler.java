package com.phenikaa.notificationService.handler;

import com.phenikaa.notificationService.adapter.WebNotificationAdapter;
import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.singleton.NotificationIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Concrete Product - Web notification handler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebNotificationHandler implements NotificationHandler {

    private final NotificationIdGenerator idGenerator = NotificationIdGenerator.getInstance();
    private final WebNotificationAdapter webAdapter; // Sử dụng Adapter Pattern

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl) {

        // Logic đặc thù cho Web notification
        String processedTitle = title;
        String processedMessage = formatWebMessage(message, type);
        String processedActionUrl = actionUrl != null ? actionUrl : "/web-notifications";

        Notification notification = Notification.builder()
                .notificationId(idGenerator.generateId())
                .senderId(senderId)
                .receiverId(receiverId)
                .title(processedTitle)
                .message(processedMessage)
                .type(type)
                .actionUrl(processedActionUrl)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        log.info("Web notification created: {} (Type: {})", notification.getTitle(), type);

        // Sử dụng Adapter Pattern để gửi web notification
        boolean webSent = webAdapter.sendNotification(receiverId.toString(), processedTitle, processedMessage, type);
        if (webSent) {
            log.info("Web notification sent successfully via WebAdapter to user: {}", receiverId);
        } else {
            log.warn("Failed to send web notification via WebAdapter to user: {}", receiverId);
        }

        return Mono.just(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEB;
    }

    @Override
    public boolean supports(NotificationType type) {
        // Web hỗ trợ tất cả notification types
        return true;
    }

    private String formatWebMessage(String message, NotificationType type) {
        switch (type) {
            case TOUR_BOOKED:
                return "[TOUR BOOKED] " + message;
            case BOOKING_CONFIRM:
                return "[CONFIRMED] " + message;
            case BOOKING_CANCELLED:
                return "[CANCELLED] " + message;
            case TOUR_REMINDER:
                return "[REMINDER] " + message;
            case TOUR_STARTED:
                return "[STARTED] " + message;
            case TOUR_COMPLETED:
                return "[COMPLETED] " + message;
            case PROMOTION:
                return "[PROMOTION] " + message;
            case PAYMENT_SUCCESS:
                return "[PAYMENT SUCCESS] " + message;
            case PAYMENT_FAILED:
                return "[PAYMENT FAILED] " + message;
            case SYSTEM:
                return "[SYSTEM] " + message;
            default:
                return message;
        }
    }
}
