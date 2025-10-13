package com.phenikaa.bookingService.controller;

import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import com.phenikaa.bookingService.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/booking/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final BookingRepository bookingRepository;

    @PostMapping("/sepay")
    public ResponseEntity<?> handleSepayWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("=== SEPAY WEBHOOK RECEIVED ===");
        System.out.println("Payload: " + payload);

        // Expected fields typically include description/content (noi_dung), amount,
        // etc.
        Object contentObj = payload.get("content"); // e.g., "Thanh toán đặt tour - BK16"
        if (contentObj == null) {
            System.out.println("Missing content field");
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing content"));
        }

        String content = String.valueOf(contentObj);
        System.out.println("Content: " + content);

        // Extract booking code from various formats
        String bookingCode = null;

        // Format 1: "Thanh toán đặt tour - BK19"
        String prefix = "Thanh toán đặt tour - ";
        if (content.startsWith(prefix)) {
            bookingCode = content.substring(prefix.length()).trim();
        }
        // Format 2: "Thanh toan dat tour BK19" (from Sepay webhook)
        else if (content.contains("Thanh toan dat tour")) {
            String[] parts = content.split("Thanh toan dat tour");
            if (parts.length > 1) {
                String afterTour = parts[1].trim();
                // Extract BK19 from the rest - handle cases like "BK23.CT"
                String[] words = afterTour.split("\\s+");
                for (String word : words) {
                    if (word.startsWith("BK") && word.length() > 2) {
                        // Remove any suffix after booking code (e.g., "BK23.CT" -> "BK23")
                        int dotIndex = word.indexOf('.');
                        if (dotIndex > 0) {
                            bookingCode = word.substring(0, dotIndex);
                        } else {
                            bookingCode = word;
                        }
                        break;
                    }
                }
            }
        }
        // Format 3: Fallback - tìm booking code ở cuối sau dấu "-"
        else {
            int idx = content.lastIndexOf('-');
            if (idx >= 0 && idx < content.length() - 1) {
                bookingCode = content.substring(idx + 1).trim();
            }
        }

        System.out.println("Extracted booking code: " + bookingCode);
        System.out.println("Original content: " + content);
        System.out.println("Looking for booking: " + bookingCode);

        if (bookingCode == null || bookingCode.isEmpty()) {
            System.out.println("Could not extract booking code from content");
            return ResponseEntity.ok(Map.of("success", false, "message", "Could not extract booking code"));
        }

        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            System.out.println("Booking not found for code: " + bookingCode);
            return ResponseEntity.ok(Map.of("success", false, "message", "Booking not found"));
        }

        System.out.println("Found booking: " + booking.getBookingCode() + ", Status: " + booking.getStatus()
                + ", Final Amount: " + booking.getFinalAmount());

        // Verify amount matches
        try {
            if (payload.get("amount") != null) {
                double paid = Double.parseDouble(String.valueOf(payload.get("amount")));
                System.out.println("Paid amount: " + paid + ", Expected: " + booking.getFinalAmount());
                if (Math.round(paid) != Math.round(booking.getFinalAmount())) {
                    System.out.println("Amount mismatch");
                    return ResponseEntity.ok(Map.of("success", false, "message", "Amount mismatch"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing amount: " + e.getMessage());
        }

        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            System.out.println("Booking " + bookingCode + " status updated to COMPLETED");
        } else {
            System.out.println("Booking " + bookingCode + " already processed, status: " + booking.getStatus());
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Payment processed successfully"));
    }
}