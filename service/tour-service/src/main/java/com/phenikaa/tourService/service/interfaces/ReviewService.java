package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.request.CreateReviewRequest;
import com.phenikaa.tourService.dto.request.UpdateReviewRequest;
import com.phenikaa.tourService.dto.response.ReviewResponse;
import com.phenikaa.tourService.dto.response.TourReviewSummary;

import java.util.List;

public interface ReviewService {

    // User functions
    ReviewResponse createReview(CreateReviewRequest request);

    ReviewResponse updateReview(UpdateReviewRequest request);

    List<ReviewResponse> getUserReviews(Integer userId);

    List<ReviewResponse> getTourReviews(Integer tourId);

    ReviewResponse getReviewById(Integer reviewId);

    // Admin functions
    List<ReviewResponse> getAllReviews();

    void deleteReview(Integer reviewId);

    // Statistics
    TourReviewSummary getTourReviewSummary(Integer tourId);

    Double getAverageRatingByTourId(Integer tourId);

    Long getReviewCountByTourId(Integer tourId);
}