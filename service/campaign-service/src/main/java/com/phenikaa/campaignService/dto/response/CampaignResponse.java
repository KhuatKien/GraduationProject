package com.phenikaa.campaignService.dto.response;

import com.phenikaa.campaignService.entity.Campaign;
import com.phenikaa.campaignService.enums.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private Integer id;
    private String name;
    private String description;
    private Double discountPercentage;
    private List<String> targetCategories;
    private Instant startDate;
    private Instant endDate;
    private CampaignStatus status;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean isExpired;
    private Boolean isActiveNow;
    private Long daysRemaining;

    public static CampaignResponse fromEntity(Campaign campaign) {
        CampaignResponse response = CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .discountPercentage(campaign.getDiscountPercentage())
                .targetCategories(campaign.getTargetCategories() != null ? campaign.getTargetCategories().stream()
                        .map(category -> category.getCategoryName())
                        .collect(Collectors.toList()) : List.of())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .isActive(campaign.getIsActive())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .createdBy(campaign.getCreatedBy())
                .updatedBy(campaign.getUpdatedBy())
                .build();

        // Compute additional fields
        Instant now = Instant.now();
        response.setIsExpired(campaign.getEndDate().isBefore(now));
        response.setIsActiveNow(campaign.getIsActive() &&
                campaign.getStartDate().isBefore(now) &&
                campaign.getEndDate().isAfter(now));

        if (campaign.getEndDate().isAfter(now)) {
            response.setDaysRemaining(java.time.Duration.between(now, campaign.getEndDate()).toDays());
        } else {
            response.setDaysRemaining(0L);
        }

        return response;
    }
}
