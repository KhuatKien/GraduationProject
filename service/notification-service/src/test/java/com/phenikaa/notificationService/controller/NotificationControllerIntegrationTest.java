package com.phenikaa.notificationService.controller;

import com.phenikaa.notificationService.dto.request.NotificationRequest;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.service.interfaces.NotificationService;
import com.phenikaa.notificationService.service.implement.NotificationServiceImpl;
import com.phenikaa.notificationService.service.AsyncNotificationService;
import com.phenikaa.notificationService.service.BatchNotificationProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Integration test cho NotificationController
 * Test Spring Boot WebFlux endpoints
 */
@WebFluxTest(NotificationController.class)
class NotificationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationServiceImpl notificationServiceImpl;

    @MockBean
    private AsyncNotificationService asyncNotificationService;

    @MockBean
    private BatchNotificationProcessor batchProcessor;

    @Test
    void testSendNotification_Success() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setSenderId(1);
        request.setReceiverId(2);
        request.setTitle("Test Notification");
        request.setMessage("This is a test notification");
        request.setType("BOOKING_CONFIRM");
        request.setActionUrl("/test");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/sendNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.notificationId").exists()
                .jsonPath("$.receiverId").isEqualTo(2)
                .jsonPath("$.title").isEqualTo("Test Notification")
                .jsonPath("$.message").isEqualTo("This is a test notification")
                .jsonPath("$.type").isEqualTo("BOOKING_CONFIRM");
    }

    @Test
    void testSendNotificationAsync_Success() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setSenderId(1);
        request.setReceiverId(2);
        request.setTitle("Test Async Notification");
        request.setMessage("This is a test async notification");
        request.setType("TOUR_REMINDER");
        request.setActionUrl("/test");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/send-async")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Async notification request submitted successfully");
    }

    @Test
    void testSendBatchNotificationAsync_Success() {
        // Given
        NotificationRequest request1 = new NotificationRequest();
        request1.setSenderId(1);
        request1.setReceiverId(2);
        request1.setTitle("Test Batch 1");
        request1.setMessage("This is batch notification 1");
        request1.setType("BOOKING_CONFIRM");
        request1.setActionUrl("/test1");

        NotificationRequest request2 = new NotificationRequest();
        request2.setSenderId(1);
        request2.setReceiverId(3);
        request2.setTitle("Test Batch 2");
        request2.setMessage("This is batch notification 2");
        request2.setType("TOUR_REMINDER");
        request2.setActionUrl("/test2");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/send-batch-async")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.List.of(request1, request2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Batch async notification request submitted successfully");
    }

    @Test
    void testSendNotificationWithTimeout_Success() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setSenderId(1);
        request.setReceiverId(2);
        request.setTitle("Test Timeout Notification");
        request.setMessage("This is a test timeout notification");
        request.setType("PAYMENT_SUCCESS");
        request.setActionUrl("/test");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/send-with-timeout?timeoutSeconds=30")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Notification with timeout request submitted successfully");
    }

    @Test
    void testSendNotificationWithCallback_Success() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setSenderId(1);
        request.setReceiverId(2);
        request.setTitle("Test Callback Notification");
        request.setMessage("This is a test callback notification");
        request.setType("PROMOTION");
        request.setActionUrl("/test");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/send-with-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Notification with callback request submitted successfully");
    }

    @Test
    void testProcessBatchWithProgress_Success() {
        // Given
        NotificationRequest request1 = new NotificationRequest();
        request1.setSenderId(1);
        request1.setReceiverId(2);
        request1.setTitle("Test Progress 1");
        request1.setMessage("This is progress notification 1");
        request1.setType("BOOKING_CONFIRM");
        request1.setActionUrl("/test1");

        NotificationRequest request2 = new NotificationRequest();
        request2.setSenderId(1);
        request2.setReceiverId(3);
        request2.setTitle("Test Progress 2");
        request2.setMessage("This is progress notification 2");
        request2.setType("TOUR_REMINDER");
        request2.setActionUrl("/test2");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/process-batch-with-progress")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.List.of(request1, request2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Batch processing with progress request submitted successfully");
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Given
        Integer receiverId = 2;

        // When & Then
        webTestClient.put()
                .uri("/api/notifications/mark-all-read/{receiverId}", receiverId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Updated 0 notifications");
    }

    @Test
    void testToggleRead_Success() {
        // Given
        Integer receiverId = 2;
        String notificationId = "NOTIF_123";

        // When & Then
        webTestClient.patch()
                .uri("/api/notifications/{receiverId}/{notificationId}/toggle", receiverId, notificationId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
