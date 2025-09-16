package com.phenikaa.bookingService.dto.response;

import com.phenikaa.bookingService.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingResponse {
    private Integer bookingId;
    private String bookingCode;
    private Integer userId;
    private Integer scheduleId;
    private Integer adultCount;
    private Integer childCount;
    private Double totalAmount;
    private Double finalAmount;
    private Double discountAmount;
    private String promotionCode;
    private String tourTitle;
    private String tourDescription;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Customer info
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Tour info
    private String tourName;
    private String tourLocation;
    private Double tourPrice;
}
