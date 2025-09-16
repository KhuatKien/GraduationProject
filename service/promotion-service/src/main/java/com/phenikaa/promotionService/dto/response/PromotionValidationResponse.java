package com.phenikaa.promotionService.dto.response;

import com.phenikaa.promotionService.entity.Promotion;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionValidationResponse {

    private boolean valid;
    private String message;
    private Promotion promotion;
    private Double discountAmount;
    private Double finalAmount;

    public static PromotionValidationResponse success(Promotion promotion, Double discountAmount, Double finalAmount) {
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

