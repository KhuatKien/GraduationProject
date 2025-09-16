package com.phenikaa.reviewService.factory;

import com.phenikaa.reviewService.decorator.CachingNotificationDecorator;
import com.phenikaa.reviewService.decorator.LoggingNotificationDecorator;
import com.phenikaa.reviewService.decorator.RetryNotificationDecorator;
import com.phenikaa.reviewService.proxy.RateLimitNotificationProxy;
import com.phenikaa.reviewService.proxy.SecurityNotificationProxy;
import com.phenikaa.reviewService.proxy.ValidationNotificationProxy;
import com.phenikaa.reviewService.entity.NotificationChannel;
import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.handler.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory Method Provider - Cung cấp factory method để tạo handler phù hợp
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFactoryProvider {

    private final NotificationHandlerFactory handlerFactory;
    private final List<NotificationHandler> handlers;

    /**
     * Factory Method - Tạo handler dựa trên channel với decorators
     */
    public NotificationHandler getHandler(NotificationChannel channel) {
        if (!handlerFactory.supports(channel)) {
            throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
        NotificationHandler baseHandler = handlerFactory.createHandler(channel);
        return wrapWithDecorators(baseHandler);
    }

    /**
     * Factory Method - Tự động chọn handler phù hợp dựa trên notification type với
     * decorators
     */
    public NotificationHandler getHandlerByType(NotificationType type) {
        // Logic mapping rõ ràng dựa trên notification type
        NotificationChannel preferredChannel = getPreferredChannel(type);

        log.info("Auto-selecting channel {} for notification type: {}", preferredChannel, type);

        NotificationHandler baseHandler = handlerFactory.createHandler(preferredChannel);
        return wrapWithDecorators(baseHandler);
    }

    /**
     * Lấy channel phù hợp nhất cho notification type
     */
    private NotificationChannel getPreferredChannel(NotificationType type) {
        return switch (type) {
            // Booking & Payment -> EMAIL (formal, detailed)
            case BOOKING_CONFIRM, BOOKING_CANCELLED, PAYMENT_SUCCESS, PAYMENT_FAILED, PROMOTION ->
                NotificationChannel.EMAIL;

            // Tour events -> SMS (urgent, short)
            case TOUR_REMINDER, TOUR_STARTED, TOUR_COMPLETED -> NotificationChannel.SMS;

            // Marketing & General -> WEB (interactive, rich)
            default -> NotificationChannel.WEB;
        };
    }

    /**
     * Factory Method - Lấy tất cả handlers hỗ trợ notification type
     */
    public List<NotificationHandler> getHandlersByType(NotificationType type) {
        return handlers.stream()
                .filter(handler -> handler.supports(type))
                .toList();
    }

    /**
     * Decorator Pattern - Wrap handler với các decorators và proxies
     * Thứ tự: Proxies -> Decorators -> Base Handler
     */
    private NotificationHandler wrapWithDecorators(NotificationHandler baseHandler) {
        log.info("Wrapping handler {} with decorators and proxies", baseHandler.getClass().getSimpleName());

        // Thứ tự: Proxies -> Decorators -> Base Handler
        NotificationHandler wrappedHandler = baseHandler;

        // === DECORATORS (inner layer) ===
        // 1. Caching decorator (outermost decorator)
        wrappedHandler = new CachingNotificationDecorator(wrappedHandler);

        // 2. Retry decorator
        wrappedHandler = new RetryNotificationDecorator(wrappedHandler);

        // 3. Logging decorator (innermost decorator)
        wrappedHandler = new LoggingNotificationDecorator(wrappedHandler);

        // === PROXIES (outer layer) ===
        // 4. Security proxy (outermost)
        wrappedHandler = new SecurityNotificationProxy(wrappedHandler);

        // 5. Rate limit proxy
        wrappedHandler = new RateLimitNotificationProxy(wrappedHandler);

        // 6. Validation proxy (innermost proxy)
        wrappedHandler = new ValidationNotificationProxy(wrappedHandler);

        log.info("Handler wrapped with decorators and proxies: {}", wrappedHandler.getClass().getSimpleName());
        return wrappedHandler;
    }
}
