package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.CreateReviewRequest;
import com.phenikaa.tourService.dto.request.UpdateReviewRequest;
import com.phenikaa.tourService.dto.response.ReviewResponse;
import com.phenikaa.tourService.dto.response.TourReviewSummary;
import com.phenikaa.tourService.service.interfaces.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/user")
@RequiredArgsConstructor
public class ReviewUserController {

    private final ReviewService reviewService;

    // User endpoints - chỉ cho phép user thực hiện
    @PostMapping("/createReview")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        try {
            ReviewResponse response = reviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/updateReview")
    public ResponseEntity<ReviewResponse> updateReview(@Valid @RequestBody UpdateReviewRequest request) {
        try {
            ReviewResponse response = reviewService.updateReview(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Integer userId) {
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Integer reviewId) {
        try {
            ReviewResponse review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Statistics endpoints - public access
    @GetMapping("/tour/{tourId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Integer tourId) {
        Double averageRating = reviewService.getAverageRatingByTourId(tourId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/tour/{tourId}/count")
    public ResponseEntity<Long> getReviewCount(@PathVariable Integer tourId) {
        Long count = reviewService.getReviewCountByTourId(tourId);
        return ResponseEntity.ok(count);
    }
}