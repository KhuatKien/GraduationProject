package com.phenikaa.tourService.dto.response;

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
public class ViewTourResponse {
    private Integer tourId;
    private String title;
    private String description;
    private String highlights;
    private Double adultPrice;
    private Double childPrice;
    private Integer duration;
    private String departure;
    private String destination;
    private String status;
    private Boolean featured;
    private Boolean isHot;
    private Boolean hasPromotion;
    private String includes;
    private String excludes;
    private String terms;
    private Integer createBy;
    private Instant createdAt;
    private Instant updatedAt;
    private ViewCategoryResponse category;

    // Review statistics
    private Double averageRating;
    private Long reviewCount;

    private List<ViewTourImageResponse> images;
    private List<ViewTourItineraryResponse> itineraries;
    private List<ViewTourScheduleResponse> schedules;

    // Campaign pricing metadata (optional)
    private Double originalAdultPrice;
    private Double originalChildPrice;
    private Double adultDiscount;
    private Double childDiscount;
    private Boolean hasCampaignDiscount;
}
