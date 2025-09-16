package com.phenikaa.promotionService.repository;

import com.phenikaa.promotionService.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Integer> {

    // Kiểm tra xem booking đã sử dụng promotion chưa
    boolean existsByBookingId(Integer bookingId);

    // Kiểm tra xem user đã sử dụng promotion này chưa
    @Query("SELECT COUNT(pu) > 0 FROM PromotionUsage pu WHERE pu.userId = :userId AND pu.promotion.promotionId = :promotionId")
    boolean existsByUserIdAndPromotionId(@Param("userId") Integer userId, @Param("promotionId") Integer promotionId);

    // Tìm promotion usage theo booking ID
    Optional<PromotionUsage> findByBookingId(Integer bookingId);

    // Tìm promotion usage theo user ID và promotion ID
    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.userId = :userId AND pu.promotion.promotionId = :promotionId")
    Optional<PromotionUsage> findByUserIdAndPromotionId(@Param("userId") Integer userId,
            @Param("promotionId") Integer promotionId);

    // Tìm tất cả promotion usage của một user
    List<PromotionUsage> findByUserId(Integer userId);

    // Tìm tất cả promotion usage của một promotion
    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.promotion.promotionId = :promotionId")
    List<PromotionUsage> findByPromotionId(@Param("promotionId") Integer promotionId);

    // Đếm số lần user đã sử dụng promotion
    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.userId = :userId AND pu.promotion.promotionId = :promotionId")
    long countByUserIdAndPromotionId(@Param("userId") Integer userId, @Param("promotionId") Integer promotionId);

    // Đếm số lần promotion đã được sử dụng
    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.promotionId = :promotionId")
    long countByPromotionId(@Param("promotionId") Integer promotionId);
}
