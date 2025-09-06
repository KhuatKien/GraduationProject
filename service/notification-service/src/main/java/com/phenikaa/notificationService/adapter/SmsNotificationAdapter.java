package com.phenikaa.notificationService.adapter;

import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.template.SmsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter cho SMS Service
 * Adapter Pattern: Chuyển đổi SmsTemplate thành NotificationAdapter interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationAdapter implements NotificationAdapter {

    private final SmsTemplate smsTemplate;

    @Override
    public boolean sendNotification(String recipient, String title, String message, NotificationType type) {
        try {
            log.info("Sending SMS to: {} via SmsAdapter", recipient);
            smsTemplate.sendSms(recipient, message);
            log.info("SMS sent successfully to: {}", recipient);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to {} via SmsAdapter: {}", recipient, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.TOUR_REMINDER ||
                type == NotificationType.TOUR_STARTED ||
                type == NotificationType.TOUR_COMPLETED ||
                type == NotificationType.BOOKING_CANCELLED;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
