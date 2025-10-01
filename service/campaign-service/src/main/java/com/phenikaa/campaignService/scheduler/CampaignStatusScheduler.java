package com.phenikaa.campaignService.scheduler;

import com.phenikaa.campaignService.service.interfaces.CampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động cập nhật status của campaigns
 * - Chạy mỗi 5 phút để kiểm tra và cập nhật status
 * - EXPIRED: khi endDate đã qua
 * - ACTIVE: khi trong khoảng thời gian hiệu lực và isActive = true
 * - DRAFT: khi chưa đến ngày bắt đầu hoặc được hồi sinh
 */
@Component
@Slf4j
public class CampaignStatusScheduler {

    private final CampaignService campaignService;

    public CampaignStatusScheduler(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    /**
     * Tự động cập nhật status của campaigns mỗi 5 phút
     * Cron expression: 0
     * 5****(mỗi 5 phút)*/

    @Scheduled(cron = "0 */5 * * * *")
    public void updateCampaignStatuses() {
        try {
            log.info("Running scheduled task: updateCampaignStatuses");
            int updatedCount = campaignService.updateExpiredCampaigns();
            log.info("Scheduled task completed - Updated {} campaigns", updatedCount);
        } catch (Exception e) {
            log.error("Error in scheduled task updateCampaignStatuses: {}", e.getMessage(), e);
        }
    }

    /**
     * Test method - chạy mỗi 30 giây để test
     * Cron expression: 0/30 * * * * * (mỗi 30 giây)
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void testCampaignStatuses() {
        try {
            log.info("Running test scheduled task: testCampaignStatuses");
            int updatedCount = campaignService.updateExpiredCampaigns();
            log.info("Test scheduled task completed - Updated {} campaigns", updatedCount);
        } catch (Exception e) {
            log.error("Error in test scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Tự động cập nhật status của campaigns mỗi ngày lúc 00:00
     * Cron expression: 0 0 0 * * * (mỗi ngày lúc 00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyCampaignStatusUpdate() {
        try {
            log.info("Running daily scheduled task: updateCampaignStatuses");
            int updatedCount = campaignService.updateExpiredCampaigns();
            log.info("Daily scheduled task completed - Updated {} campaigns", updatedCount);
        } catch (Exception e) {
            log.error("Error in daily scheduled task updateCampaignStatuses: {}", e.getMessage(), e);
        }
    }
}