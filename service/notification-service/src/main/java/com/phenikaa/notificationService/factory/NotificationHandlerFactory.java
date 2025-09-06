package com.phenikaa.notificationService.factory;

import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.handler.NotificationHandler;

/**
 * Abstract Factory - Interface cho việc tạo notification handlers
 */
public interface NotificationHandlerFactory {

    /**
     * Tạo handler dựa trên channel
     */
    NotificationHandler createHandler(NotificationChannel channel);

    /**
     * Kiểm tra xem factory có hỗ trợ channel này không
     */
    boolean supports(NotificationChannel channel);
}
