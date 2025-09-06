package com.phenikaa.notificationService.adapter;

import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.template.SmsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho SmsNotificationAdapter
 * Test Adapter Pattern implementation
 */
@ExtendWith(MockitoExtension.class)
class SmsNotificationAdapterTest {

    @Mock
    private SmsTemplate smsTemplate;

    private SmsNotificationAdapter smsAdapter;

    @BeforeEach
    void setUp() {
        smsAdapter = new SmsNotificationAdapter(smsTemplate);
    }

    @Test
    void testSendNotification_Success() {
        // Given
        String recipient = "+84123456789";
        String title = "Test SMS";
        String message = "This is a test SMS";
        NotificationType type = NotificationType.TOUR_REMINDER;

        doNothing().when(smsTemplate).sendSms(anyString(), anyString());

        // When
        boolean result = smsAdapter.sendNotification(recipient, title, message, type);

        // Then
        assertTrue(result);
        verify(smsTemplate, times(1)).sendSms(recipient, message);
    }

    @Test
    void testSendNotification_Failure() {
        // Given
        String recipient = "+84123456789";
        String title = "Test SMS";
        String message = "This is a test SMS";
        NotificationType type = NotificationType.TOUR_REMINDER;

        doThrow(new RuntimeException("SMS sending failed"))
                .when(smsTemplate).sendSms(anyString(), anyString());

        // When
        boolean result = smsAdapter.sendNotification(recipient, title, message, type);

        // Then
        assertFalse(result);
        verify(smsTemplate, times(1)).sendSms(recipient, message);
    }

    @Test
    void testSupports_SupportedTypes() {
        // Test supported notification types
        assertTrue(smsAdapter.supports(NotificationType.TOUR_REMINDER));
        assertTrue(smsAdapter.supports(NotificationType.TOUR_STARTED));
        assertTrue(smsAdapter.supports(NotificationType.TOUR_COMPLETED));
        assertTrue(smsAdapter.supports(NotificationType.BOOKING_CANCELLED));
    }

    @Test
    void testSupports_UnsupportedTypes() {
        // Test unsupported notification types
        assertFalse(smsAdapter.supports(NotificationType.BOOKING_CONFIRM));
        assertFalse(smsAdapter.supports(NotificationType.PAYMENT_SUCCESS));
        assertFalse(smsAdapter.supports(NotificationType.PAYMENT_FAILED));
        assertFalse(smsAdapter.supports(NotificationType.PROMOTION));
        assertFalse(smsAdapter.supports(NotificationType.SYSTEM));
    }

    @Test
    void testGetChannelName() {
        // When
        String channelName = smsAdapter.getChannelName();

        // Then
        assertEquals("SMS", channelName);
    }
}
