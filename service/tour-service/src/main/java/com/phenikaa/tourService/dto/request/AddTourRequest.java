package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class AddTourRequest {
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

    private List<AddTourImageRequest> images;

    private List<AddTourItineraryRequest> itineraries;

    private List<AddTourScheduleRequest> schedules;
}
