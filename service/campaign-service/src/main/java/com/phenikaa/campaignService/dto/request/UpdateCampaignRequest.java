package com.phenikaa.campaignService.dto.request;

import com.phenikaa.campaignService.enums.CampaignStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignRequest {

    @Size(max = 255, message = "Campaign name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    private Double discountPercentage;

    private List<String> targetCategories;

    private Instant startDate;

    private Instant endDate;

    private CampaignStatus status;

    private Boolean isActive;

    private String updatedBy;

    // Validation method to check if end date is after start date
    @AssertTrue(message = "End date must be after start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Skip validation if either date is null
        }
        return endDate.isAfter(startDate);
    }
}
