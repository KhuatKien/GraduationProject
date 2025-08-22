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
    private Integer maxParticipants;
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

    private List<ViewTourImageResponse> images;
    private List<ViewTourItineraryResponse> itineraries;
    private List<ViewTourScheduleResponse> schedules;
}
