package com.phenikaa.promotionService.service.interfaces;

import com.phenikaa.promotionService.dto.request.ApplyPromotionRequest;
import com.phenikaa.promotionService.dto.response.PromotionValidationResponse;
import com.phenikaa.promotionService.entity.Promotion;
import com.phenikaa.promotionService.entity.PromotionUsage;

import java.util.List;

public interface PromotionService {

    // Tạo promotion mới
    Promotion createPromotion(Promotion promotion);

    // Cập nhật promotion
    Promotion updatePromotion(Integer promotionId, Promotion promotion);

    // Xóa promotion
    void deletePromotion(Integer promotionId);

    // Lấy promotion theo ID
    Promotion getPromotionById(Integer promotionId);

    // Lấy promotion theo code
    Promotion getPromotionByCode(String promotionCode);

    // Lấy tất cả promotion
    List<Promotion> getAllPromotions();

    // Lấy promotion đang active
    List<Promotion> getActivePromotions();

    // Validate promotion code trước khi áp dụng
    PromotionValidationResponse validatePromotionCode(String promotionCode, Integer userId, Double orderAmount);

    // Áp dụng promotion code cho booking
    PromotionUsage applyPromotionCode(ApplyPromotionRequest request);

    // Kiểm tra xem user đã sử dụng promotion này chưa
    boolean hasUserUsedPromotion(Integer userId, Integer promotionId);

    // Đếm số lần user đã sử dụng promotion này
    long getUserPromotionUsageCount(Integer userId, Integer promotionId);

    // Kiểm tra xem booking đã có promotion chưa
    boolean hasBookingUsedPromotion(Integer bookingId);

    // Tính toán discount amount
    Double calculateDiscountAmount(Promotion promotion, Double orderAmount);

    // Cập nhật trạng thái promotion (ACTIVE, INACTIVE, EXPIRED)
    Promotion updatePromotionStatus(Integer promotionId, String status);

    // Tự động cập nhật status expired cho các promotion đã hết hạn
    int updateExpiredPromotions();
}
