package com.phenikaa.notificationService.handler;

import com.phenikaa.notificationService.adapter.SmsNotificationAdapter;
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
 * Concrete Product - SMS notification handler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationHandler implements NotificationHandler {

    private final NotificationIdGenerator idGenerator = NotificationIdGenerator.getInstance();
    private final SmsNotificationAdapter smsAdapter; // Sử dụng Adapter Pattern
    private final UserServiceClient userServiceClient;

    @Override
    public Mono<Notification> handleNotification(Integer senderId, Integer receiverId,
            String title, String message,
            NotificationType type, String actionUrl) {

        // Lấy phoneNumber của user trước khi tạo notification
        return userServiceClient.getUserById(receiverId)
                .flatMap(userInfo -> {
                    // Kiểm tra phoneNumber có null không
                    if (userInfo.phoneNumber() == null || userInfo.phoneNumber().trim().isEmpty()) {
                        log.error("User {} has no phone number, cannot send SMS", receiverId);
                        return Mono.error(new RuntimeException("User has no phone number"));
                    }

                    // Logic đặc thù cho SMS notification
                    String processedTitle = title;
                    String processedMessage = formatSmsMessage(message, type);
                    String processedActionUrl = actionUrl != null ? actionUrl : "/sms-notifications";

                    Notification notification = Notification.builder()
                            .notificationId(idGenerator.generateId())
                            .senderId(null) // Không có senderId cho SMS
                            .receiverId(receiverId)
                            .title(processedTitle)
                            .message(processedMessage)
                            .type(type)
                            .actionUrl(processedActionUrl)
                            .isRead(false)
                            .createdAt(Instant.now())
                            .build();

                    log.info("SMS notification created for {}: {} (Type: {})", userInfo.phoneNumber(),
                            notification.getTitle(), type);

                    // Sử dụng Adapter Pattern để gửi SMS
                    boolean smsSent = smsAdapter.sendNotification(userInfo.phoneNumber(), processedTitle,
                            processedMessage, type);
                    if (smsSent) {
                        log.info("SMS sent successfully via SmsAdapter to: {}", userInfo.phoneNumber());
                    } else {
                        log.warn("Failed to send SMS via SmsAdapter to: {}", userInfo.phoneNumber());
                    }

                    return Mono.just(notification);
                })
                .onErrorResume(error -> {
                    log.error("Failed to create SMS notification for receiverId {}: {}", receiverId,
                            error.getMessage());
                    return Mono.error(error);
                });
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean supports(NotificationType type) {
        // SMS tốt cho reminder và urgent notifications
        return type == NotificationType.TOUR_REMINDER ||
                type == NotificationType.TOUR_STARTED ||
                type == NotificationType.TOUR_COMPLETED ||
                type == NotificationType.BOOKING_CANCELLED;
    }

    private String formatSmsMessage(String message, NotificationType type) {
        // SMS cần ngắn gọn, tối đa 160 ký tự
        String shortMessage = message.length() > 100 ? message.substring(0, 97) + "..." : message;

        switch (type) {
            case TOUR_REMINDER:
                return "Reminder: " + shortMessage;
            case TOUR_STARTED:
                return "Tour started: " + shortMessage;
            case TOUR_COMPLETED:
                return "Tour completed: " + shortMessage;
            case BOOKING_CANCELLED:
                return "Booking cancelled: " + shortMessage;
            default:
                return shortMessage;
        }
    }

}
