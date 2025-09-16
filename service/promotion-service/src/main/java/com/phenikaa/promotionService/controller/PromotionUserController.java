package com.phenikaa.promotionService.controller;

import com.phenikaa.promotionService.dto.request.ApplyPromotionRequest;
import com.phenikaa.promotionService.dto.response.PromotionValidationResponse;
import com.phenikaa.promotionService.entity.Promotion;
import com.phenikaa.promotionService.entity.PromotionUsage;
import com.phenikaa.promotionService.service.interfaces.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions/user")
@RequiredArgsConstructor
@Slf4j
public class PromotionUserController {

    private final PromotionService promotionService;

    // Lấy promotion theo code (để user xem thông tin promotion)
    @GetMapping("/code/{promotionCode}")
    public ResponseEntity<Promotion> getPromotionByCode(@PathVariable String promotionCode) {
        log.info("User getting promotion with code: {}", promotionCode);
        try {
            Promotion promotion = promotionService.getPromotionByCode(promotionCode);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            log.error("Error getting promotion by code: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy tất cả promotion đang active (để user xem danh sách promotion)
    @GetMapping("/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        log.info("User getting active promotions");
        List<Promotion> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    // Validate promotion code (để user kiểm tra promotion trước khi áp dụng)
    @PostMapping("/validate")
    public ResponseEntity<PromotionValidationResponse> validatePromotionCode(
            @RequestParam String promotionCode,
            @RequestParam Integer userId,
            @RequestParam Double orderAmount) {
        log.info("User validating promotion code: {} for user: {} with order amount: {}",
                promotionCode, userId, orderAmount);
        try {
            PromotionValidationResponse response = promotionService.validatePromotionCode(
                    promotionCode, userId, orderAmount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating promotion code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Áp dụng promotion code cho booking (để user áp dụng promotion)
    @PostMapping("/apply")
    public ResponseEntity<PromotionUsage> applyPromotionCode(@Valid @RequestBody ApplyPromotionRequest request) {
        log.info("User applying promotion code: {} for booking: {}",
                request.getPromotionCode(), request.getBookingId());
        try {
            PromotionUsage promotionUsage = promotionService.applyPromotionCode(request);
            return ResponseEntity.ok(promotionUsage);
        } catch (RuntimeException e) {
            log.error("Error applying promotion code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Kiểm tra user đã sử dụng promotion chưa (để user kiểm tra lịch sử)
    @GetMapping("/check-usage")
    public ResponseEntity<Boolean> hasUserUsedPromotion(@RequestParam Integer userId,
            @RequestParam Integer promotionId) {
        log.info("User checking if user {} has used promotion {}", userId, promotionId);
        try {
            // Lấy promotion để kiểm tra userUsageLimit
            Promotion promotion = promotionService.getPromotionById(promotionId);
            if (promotion == null) {
                return ResponseEntity.ok(false);
            }

            // Kiểm tra user đã đạt giới hạn sử dụng chưa
            long userUsageCount = promotionService.getUserPromotionUsageCount(userId, promotionId);
            boolean hasReachedLimit = userUsageCount >= promotion.getUserUsageLimit();

            return ResponseEntity.ok(hasReachedLimit);
        } catch (Exception e) {
            log.error("Error checking user promotion usage: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Kiểm tra booking đã có promotion chưa (để user kiểm tra booking)
    @GetMapping("/check-booking")
    public ResponseEntity<Boolean> hasBookingUsedPromotion(@RequestParam Integer bookingId) {
        log.info("User checking if booking {} has used promotion", bookingId);
        try {
            boolean hasUsed = promotionService.hasBookingUsedPromotion(bookingId);
            return ResponseEntity.ok(hasUsed);
        } catch (Exception e) {
            log.error("Error checking booking promotion usage: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
