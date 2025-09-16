package com.phenikaa.promotionService.repository;

import com.phenikaa.promotionService.entity.Promotion;
import com.phenikaa.promotionService.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

        // Tìm promotion theo code
        Optional<Promotion> findByPromotionCode(String promotionCode);

        // Tìm promotion theo code và status
        Optional<Promotion> findByPromotionCodeAndStatus(String promotionCode, PromotionStatus status);

        // Tìm promotion đang active trong thời gian hiện tại
        @Query("SELECT p FROM Promotion p WHERE p.promotionCode = :code AND p.status = 'ACTIVE' " +
                        "AND p.startDate <= :currentTime AND p.endDate >= :currentTime")
        Optional<Promotion> findActivePromotionByCode(@Param("code") String promotionCode,
                        @Param("currentTime") Instant currentTime);

        // Tìm promotion theo status
        List<Promotion> findByStatus(PromotionStatus status);

        // Tìm promotion đang active
        @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' " +
                        "AND p.startDate <= :currentTime AND p.endDate >= :currentTime")
        List<Promotion> findActivePromotions(@Param("currentTime") Instant currentTime);
}
