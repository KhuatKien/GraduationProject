package com.phenikaa.bookingService.dto.request;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private Integer adultCount;
    private Integer childCount;
    private String promotionCode;
}
