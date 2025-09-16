package com.phenikaa.bookingService.dto.response;

import com.phenikaa.bookingService.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewBookingResponse {
    private Integer bookingId;

    private String bookingCode;

    private Integer userId; // Reference to User Service

    private Integer scheduleId; // Reference to Tour Service

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

    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    private Instant createdAt;

    private Instant updatedAt;
}
