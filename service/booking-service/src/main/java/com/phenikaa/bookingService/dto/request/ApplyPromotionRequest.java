package com.phenikaa.bookingService.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyPromotionRequest {

    @NotBlank(message = "Promotion code is required")
    private String promotionCode;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be positive")
    private Integer bookingId;

    @NotNull(message = "Order amount is required")
    @Positive(message = "Order amount must be positive")
    private Double orderAmount;
}

