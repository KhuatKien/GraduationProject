package com.phenikaa.campaignService.service.interfaces;

import com.phenikaa.campaignService.dto.request.CreateCampaignRequest;
import com.phenikaa.campaignService.dto.request.UpdateCampaignRequest;
import com.phenikaa.campaignService.dto.response.CampaignListResponse;
import com.phenikaa.campaignService.dto.response.CampaignResponse;
import com.phenikaa.campaignService.enums.CampaignStatus;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface CampaignService {

    // CRUD Operations
    CampaignResponse createCampaign(Integer userId, CreateCampaignRequest request);

    CampaignResponse getCampaignById(Integer id);

    CampaignListResponse getAllCampaigns(Pageable pageable);

    CampaignResponse updateCampaign(Integer id, Integer userId, UpdateCampaignRequest request);

    void deleteCampaign(Integer id);

    // Business Logic Operations
    CampaignResponse activateCampaign(Integer id);

    CampaignResponse pauseCampaign(Integer id);

    CampaignResponse cancelCampaign(Integer id);

    // Query Operations
    List<CampaignResponse> getActiveCampaigns();

    List<CampaignResponse> getCampaignsByStatus(CampaignStatus status);

    List<CampaignResponse> getCampaignsByCategory(String categoryName);

    List<CampaignResponse> getExpiringCampaigns(int daysBeforeExpiry);

    List<CampaignResponse> getCampaignsByDateRange(Instant startDate, Instant endDate);

    List<CampaignResponse> searchCampaignsByName(String name);

    // Statistics
    long getTotalCampaignCount();

    long getActiveCampaignCount();

    long getCampaignCountByStatus(CampaignStatus status);

    // Campaign Usage Tracking
    void incrementCampaignUsage(Integer campaignId);

    boolean isCampaignUsageLimitReached(Integer campaignId);

    // Validation
    boolean isCampaignActive(Integer campaignId);

    boolean isCampaignExpired(Integer campaignId);

    // Bulk Operations
    List<CampaignResponse> activateMultipleCampaigns(List<Integer> campaignIds);

    List<CampaignResponse> pauseMultipleCampaigns(List<Integer> campaignIds);

    void deleteMultipleCampaigns(List<Integer> campaignIds);

    // Scheduler Operations
    int updateExpiredCampaigns();
}
