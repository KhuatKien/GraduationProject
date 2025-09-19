package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.enums.TourStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchTourCriteria {
    private String title;
    private String departure;
    private String destination;

    // Status and category
    private TourStatus status;
    private String categoryName; // Keep for backward compatibility
    private List<String> categoryNames; // New field for multiple categories

    // Price range
    private Double minPrice;
    private Double maxPrice;

    // Duration range
    private Integer minDuration;
    private Integer maxDuration;

    // Boolean flags
    private Boolean featured;
    private Boolean isHot;
    private Boolean hasPromotion;

    // Date range (optional)
    private String createdAfter;
    private String createdBefore;

    // Schedule date range
    private String departureDate;
    private String returnDate;

    // Rating filter
    private Integer minRating;
    private Integer maxRating;
}
