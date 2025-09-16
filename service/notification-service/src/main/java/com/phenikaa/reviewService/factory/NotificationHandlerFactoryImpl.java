package com.phenikaa.reviewService.factory;

import com.phenikaa.reviewService.entity.NotificationChannel;
import com.phenikaa.reviewService.handler.EmailNotificationHandler;
import com.phenikaa.reviewService.handler.NotificationHandler;
import com.phenikaa.reviewService.handler.SmsNotificationHandler;
import com.phenikaa.reviewService.handler.WebNotificationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Concrete Factory - Triển khai cụ thể của NotificationHandlerFactory
 */
@Component
@RequiredArgsConstructor
public class NotificationHandlerFactoryImpl implements NotificationHandlerFactory {
    private final EmailNotificationHandler emailHandler;
    private final SmsNotificationHandler smsHandler;
    private final WebNotificationHandler webHandler;

    @Override
    public NotificationHandler createHandler(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return emailHandler;
            case SMS:
                return smsHandler;
            case WEB:
                return webHandler;
            default:
                throw new IllegalArgumentException("Unsupported notification channel: " + channel);
        }
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL ||
                channel == NotificationChannel.SMS ||
                channel == NotificationChannel.WEB;
    }
}
