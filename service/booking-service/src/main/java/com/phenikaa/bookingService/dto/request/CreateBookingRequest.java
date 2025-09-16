package com.phenikaa.bookingService.dto.request;

import com.phenikaa.bookingService.entity.BookingStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateBookingRequest {
    private String bookingCode;
    private Integer adultCount;
    private Integer childCount;
    private String promotionCode;
    private BookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
