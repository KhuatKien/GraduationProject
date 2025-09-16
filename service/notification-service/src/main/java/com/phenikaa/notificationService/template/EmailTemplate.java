package com.phenikaa.notificationService.template;

import com.phenikaa.notificationService.entity.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplate {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String message, NotificationType type) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildHtmlEmail(message, type), true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    private String buildHtmlEmail(String message, NotificationType type) {
        String color = getColorForType(type);
        String headerText = getHeaderTextForType(type);

        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Wayzy Notification</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f4f4f4;
                        }
                        .email-container {
                            background-color: #ffffff;
                            border-radius: 10px;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, %s, %s);
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 300;
                            letter-spacing: 1px;
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .message {
                            font-size: 16px;
                            line-height: 1.8;
                            margin-bottom: 25px;
                            color: #555;
                        }
                        .cta-button {
                            display: inline-block;
                            background: linear-gradient(135deg, #667eea, #764ba2);
                            color: white;
                            padding: 12px 30px;
                            text-decoration: none;
                            border-radius: 25px;
                            font-weight: 600;
                            margin: 20px 0;
                            transition: transform 0.3s ease;
                        }
                        .cta-button:hover {
                            transform: translateY(-2px);
                        }
                        .footer {
                            background-color: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            border-top: 1px solid #e9ecef;
                        }
                        .footer p {
                            margin: 5px 0;
                            color: #6c757d;
                            font-size: 14px;
                        }
                        .social-links {
                            margin: 15px 0;
                        }
                        .social-links a {
                            color: #667eea;
                            text-decoration: none;
                            margin: 0 10px;
                            font-size: 14px;
                        }
                        .divider {
                            height: 1px;
                            background: linear-gradient(to right, transparent, #ddd, transparent);
                            margin: 20px 0;
                        }
                        .status-badge {
                            display: inline-block;
                            padding: 8px 16px;
                            border-radius: 20px;
                            font-size: 12px;
                            font-weight: 600;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            margin-bottom: 15px;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>

                        <div class="content">
                            <div class="status-badge" style="background-color: %s; color: white;">
                                %s
                            </div>

                            <div class="message">
                                %s
                            </div>

                            <div style="text-align: center;">
                                <a href="https://wayzy.com" class="cta-button">Visit Wayzy</a>
                            </div>

                            <div class="divider"></div>

                            <p style="font-size: 14px; color: #666; text-align: center;">
                                If you have any questions, please don't hesitate to contact our support team.
                            </p>
                        </div>

                        <div class="footer">
                            <p><strong>Wayzy Travel Platform</strong></p>
                            <p>Your trusted partner for amazing travel experiences</p>

                            <div class="social-links">
                                <a href="https://facebook.com/wayzy">Facebook</a>
                                <a href="https://instagram.com/wayzy">Instagram</a>
                                <a href="https://twitter.com/wayzy">Twitter</a>
                            </div>

                            <p style="font-size: 12px; color: #999;">
                                © 2024 Wayzy. All rights reserved.<br>
                                This email was sent to you because you have an account with us.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, color, adjustColorBrightness(color, -20), headerText, color, getStatusText(type), message);
    }

    private String getColorForType(NotificationType type) {
        return switch (type) {
            case BOOKING_CONFIRM, PAYMENT_SUCCESS -> "#28a745";
            case BOOKING_CANCELLED, PAYMENT_FAILED -> "#dc3545";
            case PROMOTION -> "#ffc107";
            case TOUR_REMINDER, TOUR_STARTED, TOUR_COMPLETED -> "#17a2b8";
            case SYSTEM -> "#6f42c1";
            default -> "#007bff";
        };
    }

    private String getHeaderTextForType(NotificationType type) {
        return switch (type) {
            case BOOKING_CONFIRM -> "Booking Confirmed!";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case PAYMENT_SUCCESS -> "Payment Successful!";
            case PAYMENT_FAILED -> "Payment Failed";
            case PROMOTION -> "Special Offer!";
            case TOUR_REMINDER -> "Tour Reminder";
            case TOUR_STARTED -> "Tour Started!";
            case TOUR_COMPLETED -> "Tour Completed!";
            case SYSTEM -> "System Notification";
            default -> "Wayzy Notification";
        };
    }

    private String getStatusText(NotificationType type) {
        return switch (type) {
            case BOOKING_CONFIRM -> "Confirmed";
            case BOOKING_CANCELLED -> "Cancelled";
            case PAYMENT_SUCCESS -> "Success";
            case PAYMENT_FAILED -> "Failed";
            case PROMOTION -> "Promotion";
            case TOUR_REMINDER -> "Reminder";
            case TOUR_STARTED -> "Started";
            case TOUR_COMPLETED -> "Completed";
            case SYSTEM -> "System";
            default -> "Notification";
        };
    }

    private String adjustColorBrightness(String color, int percent) {
        // Simple color adjustment - in real implementation, use proper color
        // manipulation
        return color;
    }
}