package com.phenikaa.clusteringService.scheduler;

import com.phenikaa.clusteringService.service.ClusteringService;
import com.phenikaa.clusteringService.service.UserDataIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClusteringScheduler {
    
    private final UserDataIntegrationService userDataIntegrationService;
    private final ClusteringService clusteringService;
    
    /**
     * Sync user data and perform clustering daily at 2 AM
     */
    @Scheduled(cron = "${clustering.update.schedule:0 0 2 * * ?}")
    public void performDailyClustering() {
        try {
            log.info("Starting daily clustering process...");
            
            // Sync all user data first
            int syncedUsers = userDataIntegrationService.syncAllUsersData();
            log.info("Synced {} users", syncedUsers);
            
            // Perform clustering
            clusteringService.performClustering();
            log.info("Daily clustering completed successfully");
            
        } catch (Exception e) {
            log.error("Error during daily clustering: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Sync user data every 6 hours
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
    public void syncUserData() {
        try {
            log.info("Starting periodic user data sync...");
            int syncedUsers = userDataIntegrationService.syncAllUsersData();
            log.info("Periodic sync completed. Synced {} users", syncedUsers);
            
        } catch (Exception e) {
            log.error("Error during periodic user data sync: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Perform clustering when new users are added (triggered manually)
     */
    public void performClusteringOnDemand() {
        try {
            log.info("Starting on-demand clustering...");
            clusteringService.performClustering();
            log.info("On-demand clustering completed successfully");
            
        } catch (Exception e) {
            log.error("Error during on-demand clustering: {}", e.getMessage(), e);
        }
    }
}


