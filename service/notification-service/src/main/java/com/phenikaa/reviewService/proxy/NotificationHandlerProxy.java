package com.phenikaa.reviewService.proxy;

import com.phenikaa.reviewService.entity.NotificationType;
import com.phenikaa.reviewService.handler.NotificationHandler;

/**
 * Proxy interface cho NotificationHandler
 * Proxy Pattern: Kiểm soát truy cập và thêm logic bổ sung trước khi gọi handler
 * thực tế
 */
public interface NotificationHandlerProxy extends NotificationHandler {

    /**
     * Lấy handler thực tế được proxy
     * 
     * @return NotificationHandler thực tế
     */
    NotificationHandler getRealHandler();

    /**
     * Kiểm tra xem có thể xử lý request này không
     * 
     * @param senderId   ID người gửi
     * @param receiverId ID người nhận
     * @param title      Tiêu đề
     * @param message    Nội dung
     * @param type       Loại notification
     * @param actionUrl  URL hành động
     * @return true nếu có thể xử lý, false nếu không
     */
    boolean canHandle(Integer senderId, Integer receiverId, String title, String message,
            NotificationType type, String actionUrl);
}
