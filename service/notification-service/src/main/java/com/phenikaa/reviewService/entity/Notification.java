package com.phenikaa.reviewService.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Notification {
    @Id
    private String notificationId;

    private Integer senderId;

    private Integer receiverId;

    private String title;

    private String message;

    private NotificationType type; // BOOKING_CONFIRM, PROMOTION, TOUR_REMINDER, SYSTEM

    private boolean isRead;

    private String actionUrl;

    private Instant createdAt;
}
