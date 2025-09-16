package com.phenikaa.promotionService.service.implement;

import com.phenikaa.promotionService.dto.request.ApplyPromotionRequest;
import com.phenikaa.promotionService.dto.response.PromotionValidationResponse;
import com.phenikaa.promotionService.entity.Promotion;
import com.phenikaa.promotionService.entity.PromotionUsage;
import com.phenikaa.promotionService.enums.DiscountType;
import com.phenikaa.promotionService.enums.PromotionStatus;
import com.phenikaa.promotionService.repository.PromotionRepository;
import com.phenikaa.promotionService.repository.PromotionUsageRepository;
import com.phenikaa.promotionService.service.interfaces.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    @Override
    public Promotion createPromotion(Promotion promotion) {
        log.info("Creating new promotion with code: {}", promotion.getPromotionCode());
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion updatePromotion(Integer promotionId, Promotion promotion) {
        log.info("Updating promotion with ID: {}", promotionId);
        getPromotionById(promotionId); // Kiểm tra promotion tồn tại
        promotion.setPromotionId(promotionId);
        return promotionRepository.save(promotion);
    }

    @Override
    public void deletePromotion(Integer promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);
        promotionRepository.deleteById(promotionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion getPromotionById(Integer promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with ID: " + promotionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion getPromotionByCode(String promotionCode) {
        return promotionRepository.findByPromotionCode(promotionCode)
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + promotionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionValidationResponse validatePromotionCode(String promotionCode, Integer userId, Double orderAmount) {
        log.info("Validating promotion code: {} for user: {} with order amount: {}",
                promotionCode, userId, orderAmount);

        try {
            // 1. Kiểm tra promotion có tồn tại và đang active không
            Optional<Promotion> promotionOpt = promotionRepository.findActivePromotionByCode(promotionCode,
                    Instant.now());
            if (promotionOpt.isEmpty()) {
                return PromotionValidationResponse
                        .error("Promotion code not found or expired");
            }

            Promotion promotion = promotionOpt.get();

            // 2. Kiểm tra user đã đạt giới hạn sử dụng chưa
            long userUsageCount = getUserPromotionUsageCount(userId, promotion.getPromotionId());
            if (userUsageCount >= promotion.getUserUsageLimit()) {
                return PromotionValidationResponse
                        .error("You have already used this promotion code");
            }

            // 3. Kiểm tra promotion còn lượt sử dụng không
            if (promotion.getUsedCount() >= promotion.getTotalUsageLimit()) {
                return PromotionValidationResponse
                        .error("Promotion code has reached usage limit");
            }

            // 4. Kiểm tra order amount có đủ điều kiện không
            if (promotion.getMinOrderAmount() != null && orderAmount < promotion.getMinOrderAmount()) {
                return PromotionValidationResponse.error(
                        String.format("Minimum order amount required: %.2f", promotion.getMinOrderAmount()));
            }

            // 5. Tính toán discount amount
            Double discountAmount = calculateDiscountAmount(promotion, orderAmount);
            Double finalAmount = orderAmount - discountAmount;

            return PromotionValidationResponse.success(promotion,
                    discountAmount, finalAmount);

        } catch (Exception e) {
            log.error("Error validating promotion code: {}", e.getMessage());
            return PromotionValidationResponse
                    .error("Error validating promotion code: " + e.getMessage());
        }
    }

    @Override
    public PromotionUsage applyPromotionCode(ApplyPromotionRequest request) {
        log.info("Applying promotion code: {} for booking: {}",
                request.getPromotionCode(), request.getBookingId());

        // 1. Validate promotion code trước
        PromotionValidationResponse validation = validatePromotionCode(
                request.getPromotionCode(),
                request.getUserId(),
                request.getOrderAmount());

        if (!validation.isValid()) {
            throw new RuntimeException(validation.getMessage());
        }

        // 2. Kiểm tra booking đã có promotion chưa
        if (hasBookingUsedPromotion(request.getBookingId())) {
            throw new RuntimeException("This booking has already used a promotion code");
        }

        // 3. Tạo PromotionUsage record
        PromotionUsage promotionUsage = new PromotionUsage();
        promotionUsage.setPromotion(validation.getPromotion());
        promotionUsage.setUserId(request.getUserId());
        promotionUsage.setBookingId(request.getBookingId());
        promotionUsage.setOrderAmount(request.getOrderAmount());
        promotionUsage.setDiscountAmount(validation.getDiscountAmount());

        // 4. Cập nhật used count của promotion
        Promotion promotion = validation.getPromotion();
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        // 5. Lưu PromotionUsage
        PromotionUsage savedUsage = promotionUsageRepository.save(promotionUsage);

        log.info("Successfully applied promotion code: {} for booking: {}",
                request.getPromotionCode(), request.getBookingId());

        return savedUsage;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserUsedPromotion(Integer userId, Integer promotionId) {
        return promotionUsageRepository.existsByUserIdAndPromotionId(userId, promotionId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserPromotionUsageCount(Integer userId, Integer promotionId) {
        return promotionUsageRepository.countByUserIdAndPromotionId(userId, promotionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBookingUsedPromotion(Integer bookingId) {
        return promotionUsageRepository.existsByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateDiscountAmount(Promotion promotion, Double orderAmount) {
        Double discountAmount = 0.0;

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = (orderAmount * promotion.getDiscountValue()) / 100.0;
        } else if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountAmount = promotion.getDiscountValue();
        }

        // Áp dụng max discount amount nếu có
        if (promotion.getMaxDiscountAmount() != null && discountAmount > promotion.getMaxDiscountAmount()) {
            discountAmount = promotion.getMaxDiscountAmount();
        }

        // Đảm bảo discount không vượt quá order amount
        if (discountAmount > orderAmount) {
            discountAmount = orderAmount;
        }

        return discountAmount;
    }

    @Override
    public Promotion updatePromotionStatus(Integer promotionId, String status) {
        Promotion promotion = getPromotionById(promotionId);
        promotion.setStatus(PromotionStatus.valueOf(status.toUpperCase()));
        return promotionRepository.save(promotion);
    }

    @Override
    public int updateExpiredPromotions() {
        log.info("Updating expired promotions...");
        Instant now = Instant.now();

        List<Promotion> activePromotions = promotionRepository.findByStatus(PromotionStatus.ACTIVE);
        int updatedCount = 0;

        for (Promotion promotion : activePromotions) {
            if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(now)) {
                promotion.setStatus(PromotionStatus.EXPIRED);
                promotionRepository.save(promotion);
                updatedCount++;
                log.info("Updated promotion {} to EXPIRED status", promotion.getPromotionCode());
            }
        }

        log.info("Updated {} expired promotions", updatedCount);
        return updatedCount;
    }
}
