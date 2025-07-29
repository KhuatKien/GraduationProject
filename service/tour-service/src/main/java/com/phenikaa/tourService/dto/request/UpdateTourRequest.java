package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.entity.TourStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourRequest {
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
    private Integer availableSlots;
    private TourStatus status;
    private Boolean featured;
    private Boolean isHot;
    private Boolean hasPromotion;
    private String includes;
    private String excludes;
    private String terms;
    private Integer categoryId;

    private List<UpdateTourImageRequest> images;
    private List<UpdateTourItineraryRequest> itineraries;
    private List<UpdateTourScheduleRequest> schedules;
}
