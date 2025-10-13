package com.phenikaa.bookingService.service.interfaces;

public interface QrPaymentService {
    String generateQrUrl(String bookingCode, double amount);
}


