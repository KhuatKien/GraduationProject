package com.phenikaa.promotionService.scheduler;

import com.phenikaa.promotionService.service.interfaces.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động cập nhật status của promotions
 * - Chạy mỗi 5 phút để kiểm tra và cập nhật status
 * - EXPIRED: khi endDate đã qua
 * - ACTIVE: khi trong khoảng thời gian hiệu lực
 * - INACTIVE: khi bị admin tắt thủ công
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionStatusScheduler {

    private final PromotionService promotionService;

    /**
     * Tự động cập nhật status của promotions mỗi 5 phút
     * Cron expression: 0*5****(mỗi 5 phút)*/

    @Scheduled(cron = "0 */5 * * * *")
    public void updatePromotionStatuses() {
        try {
            log.info("Running scheduled task: updatePromotionStatuses");
            int updatedCount = promotionService.updateExpiredPromotions();
            log.info("Scheduled task completed - Updated {} expired promotions", updatedCount);
        } catch (Exception e) {
            log.error("Error in scheduled task updatePromotionStatuses: {}", e.getMessage(), e);
        }
    }

    /**
     * Test method - chạy mỗi 30 giây để test
     * Cron expression: 0/30 * * * * * (mỗi 30 giây)
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void testPromotionStatuses() {
        try {
            log.info("Running test scheduled task: testPromotionStatuses");
            int updatedCount = promotionService.updateExpiredPromotions();
            log.info("Test scheduled task completed - Updated {} expired promotions", updatedCount);
        } catch (Exception e) {
            log.error("Error in test scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Tự động cập nhật status của promotions mỗi ngày lúc 00:00
     * Cron expression: 0 0 0 * * * (mỗi ngày lúc 00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyPromotionStatusUpdate() {
        try {
            log.info("Running daily scheduled task: updatePromotionStatuses");
            int updatedCount = promotionService.updateExpiredPromotions();
            log.info("Daily scheduled task completed - Updated {} expired promotions", updatedCount);
        } catch (Exception e) {
            log.error("Error in daily scheduled task updatePromotionStatuses: {}", e.getMessage(), e);
        }
    }
}
