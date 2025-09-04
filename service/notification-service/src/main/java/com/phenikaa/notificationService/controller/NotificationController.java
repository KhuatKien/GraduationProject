package com.phenikaa.notificationService.controller;

import com.phenikaa.notificationService.dto.request.NotificationRequest;
import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

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
}
