package com.phenikaa.promotionService.controller;

import com.phenikaa.promotionService.entity.Promotion;
import com.phenikaa.promotionService.service.interfaces.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions/admin")
@RequiredArgsConstructor
@Slf4j
public class PromotionAdminController {

    private final PromotionService promotionService;

    // Tạo promotion mới (chỉ admin)
    @PostMapping("/createPromotion")
    public ResponseEntity<Promotion> createPromotion(@Valid @RequestBody Promotion promotion) {
        log.info("Admin creating new promotion: {}", promotion.getPromotionCode());
        try {
            Promotion createdPromotion = promotionService.createPromotion(promotion);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPromotion);
        } catch (Exception e) {
            log.error("Error creating promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Cập nhật promotion (chỉ admin)
    @PutMapping("/updatePromotion/{promotionId}")
    public ResponseEntity<Promotion> updatePromotion(@PathVariable Integer promotionId,
            @Valid @RequestBody Promotion promotion) {
        log.info("Admin updating promotion with ID: {}", promotionId);
        try {
            Promotion updatedPromotion = promotionService.updatePromotion(promotionId, promotion);
            return ResponseEntity.ok(updatedPromotion);
        } catch (Exception e) {
            log.error("Error updating promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Xóa promotion (chỉ admin)
    @DeleteMapping("/deletePromotion/{promotionId}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Integer promotionId) {
        log.info("Admin deleting promotion with ID: {}", promotionId);
        try {
            promotionService.deletePromotion(promotionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy promotion theo ID (admin có thể xem tất cả promotion)
    @GetMapping("/{promotionId}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Integer promotionId) {
        log.info("Admin getting promotion with ID: {}", promotionId);
        try {
            Promotion promotion = promotionService.getPromotionById(promotionId);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            log.error("Error getting promotion by ID: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy tất cả promotion (admin có thể xem tất cả promotion)
    @GetMapping("/getAllPromotions")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        log.info("Admin getting all promotions");
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            log.error("Error getting all promotions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Tự động cập nhật status expired cho các promotion đã hết hạn
    @PostMapping("/updateExpiredPromotions")
    public ResponseEntity<String> updateExpiredPromotions() {
        log.info("Updating expired promotions");
        try {
            int updatedCount = promotionService.updateExpiredPromotions();
            return ResponseEntity.ok("Updated " + updatedCount + " expired promotions");
        } catch (Exception e) {
            log.error("Error updating expired promotions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy promotion đang active (admin có thể xem promotion active)
    @GetMapping("/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        log.info("Admin getting active promotions");
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            log.error("Error getting active promotions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Cập nhật trạng thái promotion (chỉ admin)
    @PatchMapping("/{promotionId}/status")
    public ResponseEntity<Promotion> updatePromotionStatus(@PathVariable Integer promotionId,
            @RequestParam String status) {
        log.info("Admin updating promotion {} status to {}", promotionId, status);
        try {
            Promotion promotion = promotionService.updatePromotionStatus(promotionId, status);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            log.error("Error updating promotion status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy promotion theo code (admin có thể xem chi tiết promotion)
    @GetMapping("/code/{promotionCode}")
    public ResponseEntity<Promotion> getPromotionByCode(@PathVariable String promotionCode) {
        log.info("Admin getting promotion with code: {}", promotionCode);
        try {
            Promotion promotion = promotionService.getPromotionByCode(promotionCode);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            log.error("Error getting promotion by code: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
