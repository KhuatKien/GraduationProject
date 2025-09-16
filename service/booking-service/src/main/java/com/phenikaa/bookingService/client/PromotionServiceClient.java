package com.phenikaa.bookingService.client;

import com.phenikaa.bookingService.dto.request.ApplyPromotionRequest;
import com.phenikaa.bookingService.dto.response.PromotionValidationResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-gateway", contextId = "promotionServiceClient", path = "/promotion-service", configuration = FeignTokenInterceptor.class)
public interface PromotionServiceClient {

        // Validate promotion code
        @PostMapping("/api/promotions/user/validate")
        ResponseEntity<PromotionValidationResponse> validatePromotionCode(
                        @RequestParam("promotionCode") String promotionCode,
                        @RequestParam("userId") Integer userId,
                        @RequestParam("orderAmount") Double orderAmount);

        // Apply promotion code
        @PostMapping("/api/promotions/user/apply")
        ResponseEntity<String> applyPromotionCode(@RequestBody ApplyPromotionRequest request);

        // Check if user has used promotion
        @GetMapping("/api/promotions/user/check-usage")
        ResponseEntity<Boolean> hasUserUsedPromotion(
                        @RequestParam("userId") Integer userId,
                        @RequestParam("promotionId") Integer promotionId);

        // Check if booking has used promotion
        @GetMapping("/api/promotions/user/check-booking")
        ResponseEntity<Boolean> hasBookingUsedPromotion(@RequestParam("bookingId") Integer bookingId);

        // Get promotion by code
        @GetMapping("/api/promotions/user/code/{promotionCode}")
        ResponseEntity<Object> getPromotionByCode(@PathVariable("promotionCode") String promotionCode);
}
