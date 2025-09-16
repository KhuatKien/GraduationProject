package com.phenikaa.reviewService.template;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsTemplate {

    @Value("${sms.twilio.account-sid}")
    private String accountSid;

    @Value("${sms.twilio.auth-token}")
    private String authToken;

    @Value("${sms.twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialized with number: {}", fromPhoneNumber);
    }

    public void sendSms(String toPhoneNumber, String message) {
        try {
            // Format số điện thoại Việt Nam thành format quốc tế
            String formattedPhoneNumber = formatVietnamesePhoneNumber(toPhoneNumber);

            // Debug thông tin
            log.info("Sending SMS to: {} (formatted: {})", toPhoneNumber, formattedPhoneNumber);

            // Gửi SMS thực tế
            Message smsMessage = Message.creator(
                    new PhoneNumber(formattedPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message).create();

            // Log kết quả SMS
            log.info("SMS sent successfully to: {} (SID: {})", toPhoneNumber, smsMessage.getSid());
        } catch (Exception e) {
            // Xử lý lỗi Trial Account một cách thông minh
            if (e.getMessage() != null && e.getMessage().contains("unverified")) {
                log.warn("Trial account limitation: {}", e.getMessage());
                log.info("SMS Content (logged instead of sent): {}", message);
                log.info(
                        "To send real SMS, verify the number at: https://console.twilio.com/us1/develop/phone-numbers/manage/verified");
                return; // Không throw exception, chỉ log
            }

            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    /**
     * Format số điện thoại Việt Nam thành format quốc tế
     * Ví dụ: 0377503203 -> +84377503203
     */
    private String formatVietnamesePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Loại bỏ tất cả ký tự không phải số
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Nếu số bắt đầu bằng 0, thay thế bằng +84
        if (cleanNumber.startsWith("0")) {
            cleanNumber = "+84" + cleanNumber.substring(1);
        }
        // Nếu số bắt đầu bằng 84, thêm dấu +
        else if (cleanNumber.startsWith("84")) {
            cleanNumber = "+" + cleanNumber;
        }
        // Nếu số không có mã quốc gia, thêm +84
        else if (!cleanNumber.startsWith("+")) {
            cleanNumber = "+84" + cleanNumber;
        }

        log.info("Formatted phone number: {} -> {}", phoneNumber, cleanNumber);
        return cleanNumber;
    }
}
