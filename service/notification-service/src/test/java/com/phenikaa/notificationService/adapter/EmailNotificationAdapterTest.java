package com.phenikaa.notificationService.adapter;

import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.template.EmailTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho EmailNotificationAdapter
 * Test Adapter Pattern implementation
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    @Mock
    private EmailTemplate emailTemplate;

    private EmailNotificationAdapter emailAdapter;

    @BeforeEach
    void setUp() {
        emailAdapter = new EmailNotificationAdapter(emailTemplate);
    }

    @Test
    void testSendNotification_Success() {
        // Given
        String recipient = "test@example.com";
        String title = "Test Email";
        String message = "This is a test email";
        NotificationType type = NotificationType.BOOKING_CONFIRM;

        doNothing().when(emailTemplate).sendEmail(anyString(), anyString(), anyString(), any(NotificationType.class));

        // When
        boolean result = emailAdapter.sendNotification(recipient, title, message, type);

        // Then
        assertTrue(result);
        verify(emailTemplate, times(1)).sendEmail(recipient, title, message, type);
    }

    @Test
    void testSendNotification_Failure() {
        // Given
        String recipient = "test@example.com";
        String title = "Test Email";
        String message = "This is a test email";
        NotificationType type = NotificationType.BOOKING_CONFIRM;

        doThrow(new RuntimeException("Email sending failed"))
                .when(emailTemplate).sendEmail(anyString(), anyString(), anyString(), any(NotificationType.class));

        // When
        boolean result = emailAdapter.sendNotification(recipient, title, message, type);

        // Then
        assertFalse(result);
        verify(emailTemplate, times(1)).sendEmail(recipient, title, message, type);
    }

    @Test
    void testSupports_SupportedTypes() {
        // Test supported notification types
        assertTrue(emailAdapter.supports(NotificationType.BOOKING_CONFIRM));
        assertTrue(emailAdapter.supports(NotificationType.BOOKING_CANCELLED));
        assertTrue(emailAdapter.supports(NotificationType.PAYMENT_SUCCESS));
        assertTrue(emailAdapter.supports(NotificationType.PAYMENT_FAILED));
        assertTrue(emailAdapter.supports(NotificationType.PROMOTION));
    }

    @Test
    void testSupports_UnsupportedTypes() {
        // Test unsupported notification types
        assertFalse(emailAdapter.supports(NotificationType.TOUR_REMINDER));
        assertFalse(emailAdapter.supports(NotificationType.TOUR_STARTED));
        assertFalse(emailAdapter.supports(NotificationType.TOUR_COMPLETED));
        assertFalse(emailAdapter.supports(NotificationType.SYSTEM));
    }

    @Test
    void testGetChannelName() {
        // When
        String channelName = emailAdapter.getChannelName();

        // Then
        assertEquals("EMAIL", channelName);
    }
}
