package com.phenikaa.tourService.client;

import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "api-gateway", contextId = "campaignServiceClient", path = "/campaign-service", configuration = FeignTokenInterceptor.class)
public interface CampaignServiceClient {

    /**
     * Lấy danh sách campaign đang active
     */
    @GetMapping("/api/campaigns/active")
    ResponseEntity<List<Object>> getActiveCampaigns();

    /**
     * Lấy campaign theo ID
     */
    @GetMapping("/api/campaigns/{id}")
    ResponseEntity<Object> getCampaignById(@PathVariable("id") Integer campaignId);

    /**
     * Lấy campaign theo category
     */
    @GetMapping("/api/campaigns/category/{categoryName}")
    ResponseEntity<List<Object>> getCampaignsByCategory(@PathVariable("categoryName") String categoryName);

    /**
     * Kiểm tra campaign có active không
     */
    @GetMapping("/api/campaigns/{id}/is-active")
    ResponseEntity<Boolean> isCampaignActive(@PathVariable("id") Integer campaignId);

    /**
     * Tính toán discount từ campaign cho tour category
     */
    @PostMapping("/api/campaigns/calculate-discount")
    ResponseEntity<Double> calculateCampaignDiscount(
            @RequestParam("categoryName") String categoryName,
            @RequestParam("originalPrice") Double originalPrice);
}
