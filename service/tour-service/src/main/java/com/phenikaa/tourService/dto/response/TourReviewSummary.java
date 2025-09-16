package com.phenikaa.tourService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourReviewSummary {

    private Integer tourId;
    private Double averageRating;
    private Long totalReviews;
}
