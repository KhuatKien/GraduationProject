package com.phenikaa.notificationService.adapter;

import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.broadcaster.NotificationBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter cho Web Notification (WebSocket)
 * Adapter Pattern: Chuyển đổi NotificationBroadcaster thành NotificationAdapter
 * interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebNotificationAdapter implements NotificationAdapter {

    private final NotificationBroadcaster notificationBroadcaster;

    @Override
    public boolean sendNotification(String recipient, String title, String message, NotificationType type) {
        try {
            log.info("Sending web notification to user: {} via WebAdapter", recipient);
            // Web notification được xử lý bởi NotificationBroadcaster
            // Chỉ log thông tin, thực tế notification đã được broadcast trong handler
            log.info("Web notification prepared for user: {} - Title: {}", recipient, title);
            return true;
        } catch (Exception e) {
            log.error("Failed to prepare web notification for user {} via WebAdapter: {}", recipient, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supports(NotificationType type) {
        // Web adapter hỗ trợ tất cả loại notification
        return true;
    }

    @Override
    public String getChannelName() {
        return "WEB";
    }
}
