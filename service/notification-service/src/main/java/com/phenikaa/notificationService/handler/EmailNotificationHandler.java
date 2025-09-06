package com.phenikaa.notificationService.handler;

import com.phenikaa.notificationService.adapter.EmailNotificationAdapter;
import com.phenikaa.notificationService.client.UserServiceClient;
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
 * Concrete Product - Email notification handler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationHandler implements NotificationHandler {

    private final NotificationIdGenerator idGenerator = NotificationIdGenerator.getInstance();
    private final EmailNotificationAdapter emailAdapter; // Sử dụng Adapter Pattern
    private final UserServiceClient userServiceClient;

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl) {

        // Lấy email của user trước khi tạo notification
        return userServiceClient.getUserById(receiverId)
                .flatMap(userInfo -> {
                    // Logic đặc thù cho email notification
                    String processedTitle = title;
                    String processedMessage = formatEmailMessage(message, type);
                    String processedActionUrl = actionUrl != null ? actionUrl : "/email-notifications";

                    Notification notification = Notification.builder()
                            .notificationId(idGenerator.generateId())
                            .senderId(null) // Không có senderId cho email
                            .receiverId(receiverId)
                            .title(processedTitle)
                            .message(processedMessage)
                            .type(type)
                            .actionUrl(processedActionUrl)
                            .isRead(false)
                            .createdAt(Instant.now())
                            .build();

                    log.info("Email notification created for {}: {} (Type: {})", userInfo.email(),
                            notification.getTitle(), type);

                    // Sử dụng Adapter Pattern để gửi email
                    boolean emailSent = emailAdapter.sendNotification(userInfo.email(), processedTitle,
                            processedMessage, type);
                    if (emailSent) {
                        log.info("Email sent successfully via EmailAdapter to: {}", userInfo.email());
                    } else {
                        log.warn("Failed to send email via EmailAdapter to: {}", userInfo.email());
                    }

                    return Mono.just(notification);
                })
                .onErrorResume(error -> {
                    log.error("Failed to create email notification for receiverId {}: {}", receiverId,
                            error.getMessage());
                    return Mono.error(error);
                });
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean supports(NotificationType type) {
        // Email tốt cho booking và payment notifications
        return type == NotificationType.BOOKING_CONFIRM ||
                type == NotificationType.BOOKING_CANCELLED ||
                type == NotificationType.PAYMENT_SUCCESS ||
                type == NotificationType.PAYMENT_FAILED ||
                type == NotificationType.PROMOTION;
    }

    private String formatEmailMessage(String message, NotificationType type) {
        switch (type) {
            case BOOKING_CONFIRM:
                return "Dear Customer,\n\n" + message + "\n\nThank you for choosing us!\nBest regards,\nWayzy Team";
            case BOOKING_CANCELLED:
                return "Dear Customer,\n\n" + message
                        + "\n\nWe apologize for any inconvenience.\nBest regards,\nWayzy Team";
            case PAYMENT_SUCCESS:
                return "Payment Confirmation\n\n" + message
                        + "\n\nYour payment has been processed successfully.\nWayzy Team";
            case PAYMENT_FAILED:
                return "Payment Failed\n\n" + message + "\n\nPlease try again or contact support.\nWayzy Team";
            case PROMOTION:
                return "Special Offer!\n\n" + message + "\n\nDon't miss out on this amazing deal!\nWayzy Team";
            default:
                return message;
        }
    }

}
