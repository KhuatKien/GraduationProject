package com.phenikaa.campaignService.service.implement;

import com.phenikaa.campaignService.dto.request.CreateCampaignRequest;
import com.phenikaa.campaignService.dto.request.UpdateCampaignRequest;
import com.phenikaa.campaignService.dto.response.CampaignListResponse;
import com.phenikaa.campaignService.dto.response.CampaignResponse;
import com.phenikaa.campaignService.entity.Campaign;
import com.phenikaa.campaignService.entity.CampaignCategory;
import com.phenikaa.campaignService.enums.CampaignStatus;
import com.phenikaa.campaignService.repository.CampaignRepository;
import com.phenikaa.campaignService.service.interfaces.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignResponse createCampaign(Integer userId, CreateCampaignRequest request) {
        log.info("Creating new campaign: {} by user: {}", request.getName(), userId);
        log.info("Target categories received: {}", request.getTargetCategories());

        // Determine initial status based on dates
        Instant now = Instant.now();

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountPercentage(request.getDiscountPercentage())
                .targetCategories(new ArrayList<>()) // Initialize empty list first
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(CampaignStatus.DRAFT) // Will be updated after creation
                .isActive(request.getIsActive())
                .createdBy(userId.toString())
                .updatedBy(userId.toString())
                .build();

        // Set target categories with proper campaign reference
        List<CampaignCategory> categories = convertStringListToCampaignCategories(request.getTargetCategories(),
                campaign);
        campaign.setTargetCategories(categories);

        // Save campaign first to get ID
        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign saved with ID: {}", savedCampaign.getId());

        // Now update categories with proper campaign reference and save again
        if (request.getTargetCategories() != null && !request.getTargetCategories().isEmpty()) {
            List<CampaignCategory> updatedCategories = convertStringListToCampaignCategories(
                    request.getTargetCategories(), savedCampaign);
            savedCampaign.setTargetCategories(updatedCategories);
            savedCampaign = campaignRepository.save(savedCampaign);
            log.info("Campaign updated with {} categories: {}",
                    savedCampaign.getTargetCategories().size(),
                    savedCampaign.getTargetCategories().stream()
                            .map(CampaignCategory::getCategoryName)
                            .collect(Collectors.toList()));
        }

        // Determine and update status based on dates
        CampaignStatus determinedStatus = determineCampaignStatus(savedCampaign, now);
        if (determinedStatus != CampaignStatus.DRAFT) {
            savedCampaign.setStatus(determinedStatus);

            // If campaign is auto-started, ensure it's active
            if (determinedStatus == CampaignStatus.ACTIVE) {
                savedCampaign.setIsActive(true);
                log.info("Campaign {} auto-started and set to active", savedCampaign.getName());
            }

            savedCampaign = campaignRepository.save(savedCampaign);
            log.info("Campaign {} status auto-set to {} based on dates",
                    savedCampaign.getName(), determinedStatus);
        }

        log.info("Campaign created successfully with ID: {} and status: {}",
                savedCampaign.getId(), savedCampaign.getStatus());

        return CampaignResponse.fromEntity(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Integer id) {
        log.info("Fetching campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        return CampaignResponse.fromEntity(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignListResponse getAllCampaigns(Pageable pageable) {
        log.info("Fetching all campaigns with pagination: {}", pageable);

        Page<Campaign> campaignPage = campaignRepository.findAll(pageable);

        List<CampaignResponse> campaigns = campaignPage.getContent()
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());

        return CampaignListResponse.of(campaigns, campaignPage.getTotalElements(),
                campaignPage.getNumber(), campaignPage.getSize());
    }

    @Override
    public CampaignResponse updateCampaign(Integer id, Integer userId, UpdateCampaignRequest request) {
        log.info("Updating campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            campaign.setName(request.getName());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
        }
        if (request.getDiscountPercentage() != null) {
            campaign.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getTargetCategories() != null) {
            // Clear existing categories first
            campaign.getTargetCategories().clear();
            // Add new categories with proper campaign reference
            List<CampaignCategory> newCategories = convertStringListToCampaignCategories(request.getTargetCategories(),
                    campaign);
            campaign.getTargetCategories().addAll(newCategories);
        }
        if (request.getStartDate() != null) {
            campaign.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }

        // Auto-update status based on dates after any date changes
        Instant now = Instant.now();
        CampaignStatus currentStatus = campaign.getStatus();

        // If dates were updated, determine new status
        if (request.getStartDate() != null || request.getEndDate() != null) {
            CampaignStatus newStatus = determineCampaignStatus(campaign, now);

            if (newStatus != currentStatus) {
                campaign.setStatus(newStatus);

                // If campaign is auto-started, ensure it's active
                if (newStatus == CampaignStatus.ACTIVE) {
                    campaign.setIsActive(true);
                    log.info("Campaign {} auto-started due to date change", campaign.getId());
                }

                log.info("Campaign {} status auto-updated from {} to {} due to date change",
                        campaign.getId(), currentStatus, newStatus);
            }
        }
        if (request.getIsActive() != null) {
            campaign.setIsActive(request.getIsActive());
        }
        // Always set updatedBy - use provided value or current user
        campaign.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : userId.toString());

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign updated successfully with ID: {}", updatedCampaign.getId());

        return CampaignResponse.fromEntity(updatedCampaign);
    }

    @Override
    public void deleteCampaign(Integer id) {
        log.info("Deleting campaign with ID: {}", id);

        if (!campaignRepository.existsById(id)) {
            throw new RuntimeException("Campaign not found with ID: " + id);
        }

        campaignRepository.deleteById(id);
        log.info("Campaign deleted successfully with ID: {}", id);
    }

    @Override
    public CampaignResponse activateCampaign(Integer id) {
        log.info("Activating campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        // Check if campaign is cancelled
        if (campaign.getStatus() == CampaignStatus.CANCELLED) {
            throw new RuntimeException("Cannot activate cancelled campaign. Please update status first.");
        }

        // Check if campaign is expired (by status OR by date)
        if (campaign.getStatus() == CampaignStatus.EXPIRED) {
            throw new RuntimeException("Cannot activate expired campaign. Please update dates first.");
        }

        // Check if campaign is expired by date (regardless of status)
        Instant now = Instant.now();
        if (campaign.getEndDate().isBefore(now)) {
            throw new RuntimeException(
                    "Cannot activate campaign that has expired by date. Please update end date first.");
        }

        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setIsActive(true);
        campaign.setUpdatedBy("system"); // Set updatedBy for activation

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign activated successfully with ID: {}", updatedCampaign.getId());

        return CampaignResponse.fromEntity(updatedCampaign);
    }

    @Override
    public CampaignResponse pauseCampaign(Integer id) {
        log.info("Pausing campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        campaign.setStatus(CampaignStatus.PAUSED);
        campaign.setIsActive(false);
        campaign.setUpdatedBy("system"); // Set updatedBy for pausing

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign paused successfully with ID: {}", updatedCampaign.getId());

        return CampaignResponse.fromEntity(updatedCampaign);
    }

    @Override
    public CampaignResponse cancelCampaign(Integer id) {
        log.info("Cancelling campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        campaign.setStatus(CampaignStatus.CANCELLED);
        campaign.setIsActive(false);
        campaign.setUpdatedBy("system"); // Set updatedBy for cancellation

        Campaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Campaign cancelled successfully with ID: {}", updatedCampaign.getId());

        return CampaignResponse.fromEntity(updatedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        log.info("Fetching all active campaigns");

        return campaignRepository.findByIsActiveTrue()
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByStatus(CampaignStatus status) {
        log.info("Fetching campaigns with status: {}", status);

        return campaignRepository.findByStatus(status)
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByCategory(String categoryName) {
        log.info("Fetching campaigns for category: {}", categoryName);

        return campaignRepository.findByTargetCategory(categoryName)
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getExpiringCampaigns(int daysBeforeExpiry) {
        log.info("Fetching campaigns expiring within {} days", daysBeforeExpiry);

        Instant thresholdTime = Instant.now().plusSeconds(daysBeforeExpiry * 24 * 60 * 60);

        return campaignRepository.findExpiringCampaigns(thresholdTime)
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByDateRange(Instant startDate, Instant endDate) {
        log.info("Fetching campaigns in date range: {} to {}", startDate, endDate);

        return campaignRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> searchCampaignsByName(String name) {
        log.info("Searching campaigns by name: {}", name);

        return campaignRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCampaignCount() {
        return campaignRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveCampaignCount() {
        return campaignRepository.countByStatus(CampaignStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCampaignCountByStatus(CampaignStatus status) {
        return campaignRepository.countByStatus(status);
    }

    @Override
    public void incrementCampaignUsage(Integer campaignId) {
        log.info("Campaign usage tracking disabled - no currentUsageCount field");
        // Since currentUsageCount is removed, this method does nothing
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignUsageLimitReached(Integer campaignId) {
        // Since maxUsageCount is removed, campaigns have no usage limit
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignActive(Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + campaignId));

        Instant now = Instant.now();
        return campaign.getIsActive() &&
                campaign.getStartDate().isBefore(now) &&
                campaign.getEndDate().isAfter(now);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCampaignExpired(Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + campaignId));

        return campaign.getEndDate().isBefore(Instant.now());
    }

    @Override
    public List<CampaignResponse> activateMultipleCampaigns(List<Integer> campaignIds) {
        log.info("Activating multiple campaigns: {}", campaignIds);

        List<Campaign> campaigns = campaignRepository.findAllById(campaignIds);

        campaigns.forEach(campaign -> {
            // Check if campaign can be activated
            if (campaign.getStatus() == CampaignStatus.CANCELLED) {
                log.warn("Skipping cancelled campaign {} - cannot activate", campaign.getId());
                return;
            }
            if (campaign.getStatus() == CampaignStatus.EXPIRED) {
                log.warn("Skipping expired campaign {} - cannot activate", campaign.getId());
                return;
            }

            // Check if campaign is expired by date (regardless of status)
            Instant now = Instant.now();
            if (campaign.getEndDate().isBefore(now)) {
                log.warn("Skipping campaign {} - expired by date (endDate: {}, now: {})",
                        campaign.getId(), campaign.getEndDate(), now);
                return;
            }

            campaign.setStatus(CampaignStatus.ACTIVE);
            campaign.setIsActive(true);
            campaign.setUpdatedBy("system"); // Set updatedBy for bulk activation
        });

        List<Campaign> updatedCampaigns = campaignRepository.saveAll(campaigns);

        return updatedCampaigns.stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampaignResponse> pauseMultipleCampaigns(List<Integer> campaignIds) {
        log.info("Pausing multiple campaigns: {}", campaignIds);

        List<Campaign> campaigns = campaignRepository.findAllById(campaignIds);

        campaigns.forEach(campaign -> {
            campaign.setStatus(CampaignStatus.PAUSED);
            campaign.setIsActive(false);
            campaign.setUpdatedBy("system"); // Set updatedBy for bulk pausing
        });

        List<Campaign> updatedCampaigns = campaignRepository.saveAll(campaigns);

        return updatedCampaigns.stream()
                .map(CampaignResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMultipleCampaigns(List<Integer> campaignIds) {
        log.info("Deleting multiple campaigns: {}", campaignIds);

        campaignRepository.deleteAllById(campaignIds);
        log.info("Multiple campaigns deleted successfully");
    }

    @Override
    public int updateExpiredCampaigns() {
        log.info("Updating expired campaigns...");
        Instant now = Instant.now();

        List<Campaign> activeCampaigns = campaignRepository.findByStatus(CampaignStatus.ACTIVE);
        List<Campaign> pausedCampaigns = campaignRepository.findByStatus(CampaignStatus.PAUSED);

        int updatedCount = 0;

        // Update ACTIVE campaigns that have expired
        for (Campaign campaign : activeCampaigns) {
            if (campaign.getEndDate() != null && campaign.getEndDate().isBefore(now)) {
                campaign.setStatus(CampaignStatus.EXPIRED);
                campaign.setIsActive(false);
                campaign.setUpdatedBy("SYSTEM_SCHEDULER");
                campaignRepository.save(campaign);
                updatedCount++;
                log.info("Updated campaign {} to EXPIRED status", campaign.getName());
            }
        }

        // Update PAUSED campaigns that have expired
        for (Campaign campaign : pausedCampaigns) {
            if (campaign.getEndDate() != null && campaign.getEndDate().isBefore(now)) {
                campaign.setStatus(CampaignStatus.EXPIRED);
                campaign.setIsActive(false);
                campaign.setUpdatedBy("SYSTEM_SCHEDULER");
                campaignRepository.save(campaign);
                updatedCount++;
                log.info("Updated campaign {} to EXPIRED status", campaign.getName());
            }
        }

        // Update DRAFT campaigns that should be ACTIVE (auto-start when start date
        // arrives)
        List<Campaign> draftCampaigns = campaignRepository.findByStatus(CampaignStatus.DRAFT);
        for (Campaign campaign : draftCampaigns) {
            if (campaign.getStartDate() != null &&
                    campaign.getStartDate().isBefore(now) &&
                    campaign.getEndDate() != null &&
                    campaign.getEndDate().isAfter(now)) {

                // Auto-activate campaign when start date arrives
                campaign.setStatus(CampaignStatus.ACTIVE);
                campaign.setIsActive(true); // Ensure it's active
                campaign.setUpdatedBy("SYSTEM_SCHEDULER");
                campaignRepository.save(campaign);
                updatedCount++;
                log.info("Campaign {} auto-started (startDate: {}, now: {})",
                        campaign.getName(), campaign.getStartDate(), now);
            }
        }

        // Update EXPIRED campaigns that have been revived
        List<Campaign> expiredCampaigns = campaignRepository.findByStatus(CampaignStatus.EXPIRED);
        for (Campaign campaign : expiredCampaigns) {
            if (campaign.getEndDate() != null && campaign.getEndDate().isAfter(now)) {
                campaign.setStatus(CampaignStatus.DRAFT);
                campaign.setUpdatedBy("SYSTEM_SCHEDULER");
                campaignRepository.save(campaign);
                updatedCount++;
                log.info("Updated campaign {} to DRAFT status (revived)", campaign.getName());
            }
        }

        log.info("Updated {} campaigns", updatedCount);
        return updatedCount;
    }

    /**
     * Helper method to determine campaign status based on dates and current time
     */
    private CampaignStatus determineCampaignStatus(Campaign campaign, Instant now) {
        // If campaign has expired, set to EXPIRED
        if (campaign.getEndDate().isBefore(now)) {
            return CampaignStatus.EXPIRED;
        }

        // If campaign should be active now, set to ACTIVE (auto-start when start date
        // arrives)
        if (campaign.getStartDate().isBefore(now)) {
            return CampaignStatus.ACTIVE;
        }

        // Otherwise, keep as DRAFT
        return CampaignStatus.DRAFT;
    }

    /**
     * Helper method to convert List<String> to List<CampaignCategory>
     */
    private List<CampaignCategory> convertStringListToCampaignCategories(List<String> categoryNames,
            Campaign campaign) {
        log.info("Converting categories: {} for campaign: {}", categoryNames, campaign.getName());
        if (categoryNames == null || categoryNames.isEmpty()) {
            log.warn("Category names is null or empty");
            return List.of();
        }

        List<CampaignCategory> categories = categoryNames.stream()
                .map(categoryName -> CampaignCategory.builder()
                        .categoryName(categoryName)
                        .campaign(campaign)
                        .build())
                .collect(Collectors.toList());

        log.info("Created {} categories: {}", categories.size(), categories.stream()
                .map(CampaignCategory::getCategoryName)
                .collect(Collectors.toList()));

        return categories;
    }
}
