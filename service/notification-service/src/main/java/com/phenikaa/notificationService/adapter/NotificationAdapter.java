package com.phenikaa.notificationService.adapter;

import com.phenikaa.notificationService.entity.NotificationType;

/**
 * Adapter interface cho các notification service bên ngoài
 * Adapter Pattern: Chuyển đổi interface của các service bên ngoài thành
 * interface thống nhất
 */
public interface NotificationAdapter {

    /**
     * Gửi notification qua channel tương ứng
     * 
     * @param recipient Địa chỉ nhận (email, phone, userId)
     * @param title     Tiêu đề
     * @param message   Nội dung
     * @param type      Loại notification
     * @return true nếu gửi thành công, false nếu thất bại
     */
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
