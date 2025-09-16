package com.phenikaa.reviewService.adapter;

import com.phenikaa.reviewService.entity.NotificationType;

/**
 * Adapter interface cho các notification service bên ngoài
 * Adapter Pattern: Chuyển đổi interface của các service bên ngoài thành
 * interface thống nhất
 */
public interface NotificationAdapter {

    boolean sendNotification(String recipient, String title, String message, NotificationType type);

    /**
     * Kiểm tra xem adapter có hỗ trợ loại notification này không
     * 
     * @param type Loại notification
     * @return true nếu hỗ trợ, false nếu không
     */
    boolean supports(NotificationType type);

    /**
     * Lấy tên channel của adapter
     * 
     * @return Tên channel (EMAIL, SMS, WEB)
     */
    String getChannelName();
}
