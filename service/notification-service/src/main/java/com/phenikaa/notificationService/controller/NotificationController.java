package com.phenikaa.notificationService.controller;

import com.phenikaa.notificationService.dto.request.NotificationRequest;
import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.service.interfaces.NotificationService;
import com.phenikaa.notificationService.service.implement.NotificationServiceImpl;
import com.phenikaa.notificationService.service.AsyncNotificationService;
import com.phenikaa.notificationService.service.BatchNotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationServiceImpl notificationServiceImpl;
    private final AsyncNotificationService asyncNotificationService;
    private final BatchNotificationProcessor batchProcessor;

    @PostMapping("/sendNotification")
    public Mono<Notification> sendNotification(@RequestBody NotificationRequest req) {
        return notificationService.createNotification(
                req.getSenderId(),
                req.getReceiverId(),
                req.getTitle(),
                req.getMessage(),
                NotificationType.valueOf(req.getType()),
                req.getActionUrl());
    }

    @PutMapping("/mark-all-read/{receiverId}")
    public Mono<ResponseEntity<String>> markAllAsRead(@PathVariable Integer receiverId) {
        return notificationService.markAllAsReadAndPublish(receiverId)
                .map(count -> ResponseEntity.ok("Updated " + count + " notifications"))
                .defaultIfEmpty(ResponseEntity.ok("No notifications to update"));
    }

    @PatchMapping("/{receiverId}/{notificationId}/toggle")
    public Mono<ResponseEntity<Notification>> toggleRead(
            @PathVariable Integer receiverId,
            @PathVariable String notificationId) {

        return notificationService.toggleReadAndPublish(receiverId, notificationId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Tạo notification với channel cụ thể
     */
    @PostMapping("/send/{channel}")
    public Mono<ResponseEntity<Notification>> sendWithChannel(
            @PathVariable NotificationChannel channel,
            @RequestBody NotificationRequest req) {

        return notificationService.createNotificationWithChannel(
                req.getSenderId(),
                req.getReceiverId(),
                req.getTitle(),
                req.getMessage(),
                NotificationType.valueOf(req.getType()),
                req.getActionUrl(),
                channel).map(ResponseEntity::ok);
    }

    // === ASYNC ENDPOINTS ===

    /**
     * Gửi notification bất đồng bộ
     */
    @PostMapping("/send-async")
    public ResponseEntity<String> sendNotificationAsync(@RequestBody NotificationRequest req) {
        log.info("Received async notification request for receiver: {}", req.getReceiverId());

        notificationServiceImpl.createNotificationAsync(
                req.getSenderId(),
                req.getReceiverId(),
                req.getTitle(),
                req.getMessage(),
                NotificationType.valueOf(req.getType()),
                req.getActionUrl()).whenComplete((notification, throwable) -> {
                    if (throwable != null) {
                        log.error("Async notification failed: {}", throwable.getMessage());
                    } else {
                        log.info("Async notification completed: {}", notification.getNotificationId());
                    }
                });

        return ResponseEntity.ok("Async notification request submitted successfully");
    }

    /**
     * Gửi nhiều notification bất đồng bộ
     */
    @PostMapping("/send-batch-async")
    public ResponseEntity<String> sendBatchNotificationAsync(
            @RequestBody java.util.List<NotificationRequest> requests) {
        log.info("Received batch async notification request for {} notifications", requests.size());

        // Convert to AsyncNotificationService.NotificationRequest
        java.util.List<AsyncNotificationService.NotificationRequest> asyncRequests = requests.stream()
                .map(req -> new AsyncNotificationService.NotificationRequest(
                        req.getSenderId(),
                        req.getReceiverId(),
                        req.getTitle(),
                        req.getMessage(),
                        NotificationType.valueOf(req.getType()),
                        req.getActionUrl()))
                .collect(java.util.stream.Collectors.toList());

        notificationServiceImpl.createMultipleNotificationsAsync(asyncRequests)
                .whenComplete((notifications, throwable) -> {
                    if (throwable != null) {
                        log.error("Batch async notification failed: {}", throwable.getMessage());
                    } else {
                        log.info("Batch async notification completed: {} notifications", notifications.size());
                    }
                });

        return ResponseEntity.ok("Batch async notification request submitted successfully");
    }

    /**
     * Gửi notification với timeout
     */
    @PostMapping("/send-with-timeout")
    public ResponseEntity<String> sendNotificationWithTimeout(@RequestBody NotificationRequest req,
            @RequestParam(defaultValue = "30") long timeoutSeconds) {
        log.info("Received notification with timeout request for receiver: {} (timeout: {}s)",
                req.getReceiverId(), timeoutSeconds);

        asyncNotificationService.sendNotificationWithTimeout(
                req.getSenderId(),
                req.getReceiverId(),
                req.getTitle(),
                req.getMessage(),
                NotificationType.valueOf(req.getType()),
                req.getActionUrl(),
                timeoutSeconds).whenComplete((notification, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            log.warn("Notification timeout after {}s for receiver: {}", timeoutSeconds,
                                    req.getReceiverId());
                        } else {
                            log.error("Notification with timeout failed: {}", throwable.getMessage());
                        }
                    } else {
                        log.info("Notification with timeout completed: {}", notification.getNotificationId());
                    }
                });

        return ResponseEntity.ok("Notification with timeout request submitted successfully");
    }

    /**
     * Xử lý batch notification với progress tracking
     */
    @PostMapping("/process-batch-with-progress")
    public ResponseEntity<String> processBatchWithProgress(@RequestBody java.util.List<NotificationRequest> requests) {
        log.info("Received batch processing with progress request for {} notifications", requests.size());

        // Convert to AsyncNotificationService.NotificationRequest
        java.util.List<AsyncNotificationService.NotificationRequest> asyncRequests = requests.stream()
                .map(req -> new AsyncNotificationService.NotificationRequest(
                        req.getSenderId(),
                        req.getReceiverId(),
                        req.getTitle(),
                        req.getMessage(),
                        NotificationType.valueOf(req.getType()),
                        req.getActionUrl()))
                .collect(java.util.stream.Collectors.toList());

        // Progress callback
        java.util.function.Consumer<BatchNotificationProcessor.BatchProgress> progressCallback = progress -> {
            log.info("Batch Progress: {}", progress);
        };

        notificationServiceImpl.processBatchWithProgress(asyncRequests, progressCallback)
                .whenComplete((notifications, throwable) -> {
                    if (throwable != null) {
                        log.error("Batch processing with progress failed: {}", throwable.getMessage());
                    } else {
                        log.info("Batch processing with progress completed: {} notifications", notifications.size());
                    }
                });

        return ResponseEntity.ok("Batch processing with progress request submitted successfully");
    }

    /**
     * Gửi notification với callback
     */
    @PostMapping("/send-with-callback")
    public ResponseEntity<String> sendNotificationWithCallback(@RequestBody NotificationRequest req) {
        log.info("Received notification with callback request for receiver: {}", req.getReceiverId());

        asyncNotificationService.sendNotificationWithCallback(
                req.getSenderId(),
                req.getReceiverId(),
                req.getTitle(),
                req.getMessage(),
                NotificationType.valueOf(req.getType()),
                req.getActionUrl(),
                notification -> log.info("Callback - Notification success: {}", notification.getNotificationId()),
                throwable -> log.error("Callback - Notification failed: {}", throwable.getMessage()));

        return ResponseEntity.ok("Notification with callback request submitted successfully");
    }

}
