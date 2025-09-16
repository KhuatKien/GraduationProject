package com.phenikaa.reviewService.adapter;

import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.template.EmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter cho Email Service
 * Adapter Pattern: Chuyển đổi EmailTemplate thành NotificationAdapter interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationAdapter implements NotificationAdapter {

    private final EmailTemplate emailTemplate;

    @Override
    public boolean sendNotification(String recipient, String title, String message, NotificationType type) {
        try {
            log.info("Sending email to: {} via EmailAdapter", recipient);
            emailTemplate.sendEmail(recipient, title, message, type);
            log.info("Email sent successfully to: {}", recipient);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {} via EmailAdapter: {}", recipient, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.BOOKING_CONFIRM ||
                type == NotificationType.BOOKING_CANCELLED ||
                type == NotificationType.PAYMENT_SUCCESS ||
                type == NotificationType.PAYMENT_FAILED ||
                type == NotificationType.PROMOTION;
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
