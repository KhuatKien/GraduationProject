package com.phenikaa.tourService.scheduler;

import com.phenikaa.tourService.service.interfaces.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động cập nhật status của tour schedules
 * - Chạy mỗi 5 phút để kiểm tra và cập nhật status
 * - EXPIRED: khi ngày khởi hành đã qua
 * - FULL: khi available slots = 0
 * - AVAILABLE: khi còn slot và chưa hết hạn
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleStatusScheduler {

    private final ScheduleService scheduleService;

    /**
     * Tự động cập nhật status của schedules mỗi 5 phút
     * Cron expression: 0 *5 * * * * (mỗi 5 phút)
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void updateScheduleStatuses() {
        try {
            log.info("Running scheduled task: updateScheduleStatuses");
            scheduleService.updateScheduleStatuses();
        } catch (Exception e) {
            log.error("Error in scheduled task updateScheduleStatuses: {}", e.getMessage(), e);
        }
    }

    /**
     * Test method - chạy mỗi 30 giây để test
     * Cron expression: 0/30 * * * * * (mỗi 30 giây)
     * TEMPORARILY DISABLED due to CHECK constraint issue
     */
    // @Scheduled(cron = "0/30 * * * * *")
    public void testScheduleStatuses() {
        try {
            log.info("Running test scheduled task: testScheduleStatuses");
            scheduleService.updateScheduleStatuses();
        } catch (Exception e) {
            log.error("Error in test scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Tự động cập nhật status của schedules mỗi ngày lúc 00:00
     * Cron expression: 0 0 0 * * * (mỗi ngày lúc 00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyScheduleStatusUpdate() {
        try {
            log.info("Running daily scheduled task: updateScheduleStatuses");
            scheduleService.updateScheduleStatuses();
        } catch (Exception e) {
            log.error("Error in daily scheduled task updateScheduleStatuses: {}", e.getMessage(), e);
        }
    }
}