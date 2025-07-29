package com.phenikaa.tourService.dto.request;

import lombok.Data;

@Data
public class SearchTourRequest {

        private Integer tourId;
        private String title;
        private String description;
        private Double adultPrice;
        private Double childPrice;
        private Integer duration;
        private String departure;
        private String destination;
        private Integer availableSlots;
        private String status;
        private Boolean featured;
        private Boolean isHot;
        private Boolean hasPromotion;
}
