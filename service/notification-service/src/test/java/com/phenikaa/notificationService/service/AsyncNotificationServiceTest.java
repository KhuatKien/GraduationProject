package com.phenikaa.notificationService.service;

import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.factory.NotificationFactoryProvider;
import com.phenikaa.notificationService.handler.NotificationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AsyncNotificationService
 * Test Concurrency Pattern với CompletableFuture
 */
@ExtendWith(MockitoExtension.class)
class AsyncNotificationServiceTest {

    @Mock
    private NotificationFactoryProvider factoryProvider;

    @Mock
    private NotificationHandler notificationHandler;

    @Mock
    private Executor notificationExecutor;

    @Mock
    private Executor emailExecutor;

    private AsyncNotificationService asyncNotificationService;

    @BeforeEach
    void setUp() {
        asyncNotificationService = new AsyncNotificationService(factoryProvider, notificationExecutor, emailExecutor);
    }

    @Test
    void testSendNotificationAsync_Success() throws Exception {
        // Given
        Integer senderId = 1;
        Integer receiverId = 2;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.BOOKING_CONFIRM;

        Notification expectedNotification = Notification.builder()
                .notificationId("NOTIF_123")
                .senderId(senderId)
                .receiverId(receiverId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        when(factoryProvider.getHandlerByType(type)).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(expectedNotification));

        // When
        CompletableFuture<Notification> future = asyncNotificationService.sendNotificationAsync(
                senderId, receiverId, title, message, type, null);

        // Then
        Notification result = future.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(expectedNotification.getNotificationId(), result.getNotificationId());
        assertEquals(receiverId, result.getReceiverId());
        assertEquals(title, result.getTitle());
        assertEquals(message, result.getMessage());
        assertEquals(type, result.getType());
    }

    @Test
    void testSendNotificationAsync_Failure() {
        // Given
        Integer senderId = 1;
        Integer receiverId = 2;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.BOOKING_CONFIRM;

        when(factoryProvider.getHandlerByType(type)).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Handler failed")));

        // When
        CompletableFuture<Notification> future = asyncNotificationService.sendNotificationAsync(
                senderId, receiverId, title, message, type, null);

        // Then
        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void testSendNotificationWithCallback_Success() {
        // Given
        Integer senderId = 1;
        Integer receiverId = 2;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.BOOKING_CONFIRM;

        Notification expectedNotification = Notification.builder()
                .notificationId("NOTIF_123")
                .senderId(senderId)
                .receiverId(receiverId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        when(factoryProvider.getHandlerByType(type)).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(expectedNotification));

        // Mock callbacks
        java.util.function.Consumer<Notification> onSuccess = mock(java.util.function.Consumer.class);
        java.util.function.Consumer<Throwable> onError = mock(java.util.function.Consumer.class);

        // When
        asyncNotificationService.sendNotificationWithCallback(
                senderId, receiverId, title, message, type, null, onSuccess, onError);

        // Wait a bit for async processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(onSuccess, times(1)).accept(any(Notification.class));
        verify(onError, never()).accept(any(Throwable.class));
    }

    @Test
    void testSendMultipleNotificationsAsync_Success() throws Exception {
        // Given
        List<AsyncNotificationService.NotificationRequest> requests = List.of(
                new AsyncNotificationService.NotificationRequest(1, 2, "Title 1", "Message 1",
                        NotificationType.BOOKING_CONFIRM, null),
                new AsyncNotificationService.NotificationRequest(1, 3, "Title 2", "Message 2",
                        NotificationType.TOUR_REMINDER, null));

        Notification notification1 = Notification.builder()
                .notificationId("NOTIF_1")
                .senderId(1)
                .receiverId(2)
                .title("Title 1")
                .message("Message 1")
                .type(NotificationType.BOOKING_CONFIRM)
                .isRead(false)
                .build();

        Notification notification2 = Notification.builder()
                .notificationId("NOTIF_2")
                .senderId(1)
                .receiverId(3)
                .title("Title 2")
                .message("Message 2")
                .type(NotificationType.TOUR_REMINDER)
                .isRead(false)
                .build();

        when(factoryProvider.getHandlerByType(any())).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(notification1))
                .thenReturn(Mono.just(notification2));

        // When
        CompletableFuture<List<Notification>> future = asyncNotificationService
                .sendMultipleNotificationsAsync(requests);

        // Then
        List<Notification> result = future.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(n -> n.getNotificationId().equals("NOTIF_1")));
        assertTrue(result.stream().anyMatch(n -> n.getNotificationId().equals("NOTIF_2")));
    }

    @Test
    void testSendNotificationWithTimeout_Success() throws Exception {
        // Given
        Integer senderId = 1;
        Integer receiverId = 2;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.BOOKING_CONFIRM;
        long timeoutSeconds = 5;

        Notification expectedNotification = Notification.builder()
                .notificationId("NOTIF_123")
                .senderId(senderId)
                .receiverId(receiverId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        when(factoryProvider.getHandlerByType(type)).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(expectedNotification));

        // When
        CompletableFuture<Notification> future = asyncNotificationService.sendNotificationWithTimeout(
                senderId, receiverId, title, message, type, null, timeoutSeconds);

        // Then
        Notification result = future.get(10, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(expectedNotification.getNotificationId(), result.getNotificationId());
    }

    @Test
    void testSendNotificationWithTimeout_Timeout() {
        // Given
        Integer senderId = 1;
        Integer receiverId = 2;
        String title = "Test Notification";
        String message = "This is a test notification";
        NotificationType type = NotificationType.BOOKING_CONFIRM;
        long timeoutSeconds = 1; // Very short timeout

        when(factoryProvider.getHandlerByType(type)).thenReturn(notificationHandler);
        when(notificationHandler.handleNotification(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.delay(java.time.Duration.ofSeconds(2)) // Delay longer than timeout
                        .then(Mono.just(Notification.builder().build())));

        // When
        CompletableFuture<Notification> future = asyncNotificationService.sendNotificationWithTimeout(
                senderId, receiverId, title, message, type, null, timeoutSeconds);

        // Then
        assertThrows(java.util.concurrent.TimeoutException.class, () -> future.get(5, TimeUnit.SECONDS));
    }
}
