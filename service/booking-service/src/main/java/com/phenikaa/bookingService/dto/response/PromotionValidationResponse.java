package com.phenikaa.bookingService.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionValidationResponse {

    private boolean valid;
    private String message;
    private PromotionDetails promotion;
    private Double discountAmount;
    private Double finalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PromotionDetails {
        private Integer promotionId;
        private String promotionCode;
        private String title;
        private String description;
        private String discountType;
        private Double discountValue;
        private Double maxDiscountAmount;
        private Double minOrderAmount;
        private Integer totalUsageLimit;
        private Integer usedCount;
        private Integer userUsageLimit;
        private String status;
    }

    public static PromotionValidationResponse success(PromotionDetails promotion, Double discountAmount,
            Double finalAmount) {
        return PromotionValidationResponse.builder()
                .valid(true)
                .message("Promotion code is valid")
                .promotion(promotion)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    public static PromotionValidationResponse error(String message) {
        return PromotionValidationResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}

