package com.phenikaa.notificationService.factory;

import com.phenikaa.notificationService.entity.NotificationChannel;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.handler.NotificationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho NotificationFactoryProvider
 * Test Factory Method + Abstract Factory Pattern
 */
@ExtendWith(MockitoExtension.class)
class NotificationFactoryProviderTest {

    @Mock
    private NotificationHandlerFactory handlerFactory;

    @Mock
    private NotificationHandler emailHandler;

    @Mock
    private NotificationHandler smsHandler;

    @Mock
    private NotificationHandler webHandler;

    private NotificationFactoryProvider factoryProvider;

    @BeforeEach
    void setUp() {
        factoryProvider = new NotificationFactoryProvider(handlerFactory,
                List.of(emailHandler, smsHandler, webHandler));
    }

    @Test
    void testGetHandler_EmailChannel() {
        // Given
        NotificationChannel channel = NotificationChannel.EMAIL;
        when(handlerFactory.supports(channel)).thenReturn(true);
        when(handlerFactory.createHandler(channel)).thenReturn(emailHandler);

        // When
        NotificationHandler result = factoryProvider.getHandler(channel);

        // Then
        assertNotNull(result);
        assertEquals(emailHandler, result);
        verify(handlerFactory, times(1)).supports(channel);
        verify(handlerFactory, times(1)).createHandler(channel);
    }

    @Test
    void testGetHandler_UnsupportedChannel() {
        // Given
        NotificationChannel channel = NotificationChannel.EMAIL;
        when(handlerFactory.supports(channel)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> factoryProvider.getHandler(channel));
        verify(handlerFactory, times(1)).supports(channel);
        verify(handlerFactory, never()).createHandler(any());
    }

    @Test
    void testGetHandlerByType_BookingConfirm() {
        // Given
        NotificationType type = NotificationType.BOOKING_CONFIRM;
        when(handlerFactory.createHandler(NotificationChannel.EMAIL)).thenReturn(emailHandler);

        // When
        NotificationHandler result = factoryProvider.getHandlerByType(type);

        // Then
        assertNotNull(result);
        assertEquals(emailHandler, result);
        verify(handlerFactory, times(1)).createHandler(NotificationChannel.EMAIL);
    }

    @Test
    void testGetHandlerByType_TourReminder() {
        // Given
        NotificationType type = NotificationType.TOUR_REMINDER;
        when(handlerFactory.createHandler(NotificationChannel.SMS)).thenReturn(smsHandler);

        // When
        NotificationHandler result = factoryProvider.getHandlerByType(type);

        // Then
        assertNotNull(result);
        assertEquals(smsHandler, result);
        verify(handlerFactory, times(1)).createHandler(NotificationChannel.SMS);
    }

    @Test
    void testGetHandlerByType_System() {
        // Given
        NotificationType type = NotificationType.SYSTEM;
        when(handlerFactory.createHandler(NotificationChannel.WEB)).thenReturn(webHandler);

        // When
        NotificationHandler result = factoryProvider.getHandlerByType(type);

        // Then
        assertNotNull(result);
        assertEquals(webHandler, result);
        verify(handlerFactory, times(1)).createHandler(NotificationChannel.WEB);
    }

    @Test
    void testGetHandlersByType() {
        // Given
        NotificationType type = NotificationType.BOOKING_CONFIRM;
        when(emailHandler.supports(type)).thenReturn(true);
        when(smsHandler.supports(type)).thenReturn(false);
        when(webHandler.supports(type)).thenReturn(true);

        // When
        List<NotificationHandler> result = factoryProvider.getHandlersByType(type);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(emailHandler));
        assertTrue(result.contains(webHandler));
        assertFalse(result.contains(smsHandler));
    }
}
