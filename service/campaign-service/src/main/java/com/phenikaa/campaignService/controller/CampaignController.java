package com.phenikaa.campaignService.controller;

import com.phenikaa.campaignService.dto.request.CreateCampaignRequest;
import com.phenikaa.campaignService.dto.request.UpdateCampaignRequest;
import com.phenikaa.campaignService.dto.response.CampaignListResponse;
import com.phenikaa.campaignService.dto.response.CampaignResponse;
import com.phenikaa.campaignService.enums.CampaignStatus;
import com.phenikaa.campaignService.service.interfaces.CampaignService;
import com.phenikaa.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;
    private final JwtUtil jwtUtil;

    // CRUD Operations

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateCampaignRequest request) {
        log.info("Creating new campaign: {}", request.getName());

        // Extract userId from JWT token
        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        CampaignResponse response = campaignService.createCampaign(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Integer id) {
        log.info("Fetching campaign with ID: {}", id);
        CampaignResponse response = campaignService.getCampaignById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CampaignListResponse> getAllCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching all campaigns - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        CampaignListResponse response = campaignService.getAllCampaigns(pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateCampaignRequest request) {
        log.info("Updating campaign with ID: {}", id);

        // Extract userId from JWT token
        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        CampaignResponse response = campaignService.updateCampaign(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Integer id) {
        log.info("Deleting campaign with ID: {}", id);
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    // Campaign Status Management

    @PutMapping("/{id}/activate")
    public ResponseEntity<CampaignResponse> activateCampaign(@PathVariable Integer id) {
        log.info("Activating campaign with ID: {}", id);
        CampaignResponse response = campaignService.activateCampaign(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<CampaignResponse> pauseCampaign(@PathVariable Integer id) {
        log.info("Pausing campaign with ID: {}", id);
        CampaignResponse response = campaignService.pauseCampaign(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<CampaignResponse> cancelCampaign(@PathVariable Integer id) {
        log.info("Cancelling campaign with ID: {}", id);
        CampaignResponse response = campaignService.cancelCampaign(id);
        return ResponseEntity.ok(response);
    }

    // Query Operations

    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns() {
        log.info("Fetching all active campaigns");
        List<CampaignResponse> response = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CampaignResponse>> getCampaignsByStatus(@PathVariable CampaignStatus status) {
        log.info("Fetching campaigns with status: {}", status);
        List<CampaignResponse> response = campaignService.getCampaignsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<CampaignResponse>> getCampaignsByCategory(@PathVariable String categoryName) {
        log.info("Fetching campaigns for category: {}", categoryName);
        List<CampaignResponse> response = campaignService.getCampaignsByCategory(categoryName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<CampaignResponse>> getExpiringCampaigns(
            @RequestParam(defaultValue = "7") int daysBeforeExpiry) {
        log.info("Fetching campaigns expiring within {} days", daysBeforeExpiry);
        List<CampaignResponse> response = campaignService.getExpiringCampaigns(daysBeforeExpiry);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CampaignResponse>> getCampaignsByDateRange(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        log.info("Fetching campaigns in date range: {} to {}", startDate, endDate);
        List<CampaignResponse> response = campaignService.getCampaignsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CampaignResponse>> searchCampaignsByName(@RequestParam String name) {
        log.info("Searching campaigns by name: {}", name);
        List<CampaignResponse> response = campaignService.searchCampaignsByName(name);
        return ResponseEntity.ok(response);
    }

    // Statistics

    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalCampaignCount() {
        long count = campaignService.getTotalCampaignCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active")
    public ResponseEntity<Long> getActiveCampaignCount() {
        long count = campaignService.getActiveCampaignCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getCampaignCountByStatus(@PathVariable CampaignStatus status) {
        long count = campaignService.getCampaignCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    // Campaign Usage Tracking

    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<Void> incrementCampaignUsage(@PathVariable Integer id) {
        log.info("Incrementing usage count for campaign ID: {}", id);
        campaignService.incrementCampaignUsage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/usage-limit-reached")
    public ResponseEntity<Boolean> isCampaignUsageLimitReached(@PathVariable Integer id) {
        boolean limitReached = campaignService.isCampaignUsageLimitReached(id);
        return ResponseEntity.ok(limitReached);
    }

    @GetMapping("/{id}/is-active")
    public ResponseEntity<Boolean> isCampaignActive(@PathVariable Integer id) {
        boolean isActive = campaignService.isCampaignActive(id);
        return ResponseEntity.ok(isActive);
    }

    @GetMapping("/{id}/is-expired")
    public ResponseEntity<Boolean> isCampaignExpired(@PathVariable Integer id) {
        boolean isExpired = campaignService.isCampaignExpired(id);
        return ResponseEntity.ok(isExpired);
    }

    // Bulk Operations

    @PutMapping("/bulk/activate")
    public ResponseEntity<List<CampaignResponse>> activateMultipleCampaigns(@RequestBody List<Integer> campaignIds) {
        log.info("Activating multiple campaigns: {}", campaignIds);
        List<CampaignResponse> response = campaignService.activateMultipleCampaigns(campaignIds);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/bulk/pause")
    public ResponseEntity<List<CampaignResponse>> pauseMultipleCampaigns(@RequestBody List<Integer> campaignIds) {
        log.info("Pausing multiple campaigns: {}", campaignIds);
        List<CampaignResponse> response = campaignService.pauseMultipleCampaigns(campaignIds);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteMultipleCampaigns(@RequestBody List<Integer> campaignIds) {
        log.info("Deleting multiple campaigns: {}", campaignIds);
        campaignService.deleteMultipleCampaigns(campaignIds);
        return ResponseEntity.noContent().build();
    }

    // Endpoint để test manual cập nhật status campaigns
    @PostMapping("/admin/updateCampaignStatuses")
    public ResponseEntity<?> updateCampaignStatuses() {
        try {
            log.info("Manual update campaign statuses requested");
            int updatedCount = campaignService.updateExpiredCampaigns();
            return ResponseEntity
                    .ok("Campaign statuses updated successfully. Updated " + updatedCount + " campaigns");
        } catch (Exception e) {
            log.error("Error updating campaign statuses: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error updating campaign statuses: " + e.getMessage());
        }
    }

    @PostMapping("/calculate-discount")
    public ResponseEntity<Double> calculateCampaignDiscount(
            @RequestParam("categoryName") String categoryName,
            @RequestParam("originalPrice") Double originalPrice) {
        log.info("Calculating campaign discount for category: {} with price: {}", categoryName, originalPrice);

        Double discountAmount = campaignService.calculateCampaignDiscount(categoryName, originalPrice);
        return ResponseEntity.ok(discountAmount);
    }
}
