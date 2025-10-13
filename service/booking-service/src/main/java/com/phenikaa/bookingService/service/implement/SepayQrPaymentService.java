package com.phenikaa.bookingService.service.implement;

import com.phenikaa.bookingService.config.SepayProperties;
import com.phenikaa.bookingService.service.interfaces.QrPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@RequiredArgsConstructor
public class SepayQrPaymentService implements QrPaymentService {
    private final SepayProperties sepayProperties;

    @Override
    public String generateQrUrl(String bookingCode, double amount) {
        String account = sepayProperties.getAccount();
        String bank = sepayProperties.getBank();
        // Tạo description chứa booking code để webhook có thể tìm thấy
        String description = sepayProperties.getDescription() + " - " + bookingCode;
        try {
            String encodedDes = URLEncoder.encode(description, "UTF-8");
            // Sepay VietQR dynamic image URL pattern với template=compact
            return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%.0f&des=%s&template=compact",
                    account, bank, Math.floor(amount), encodedDes);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode description", e);
        }
    }
}
