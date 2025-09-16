package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.response.ReviewResponse;
import com.phenikaa.tourService.service.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/admin")
@RequiredArgsConstructor
public class ReviewAdminController {

    private final ReviewService reviewService;

    // Admin endpoints - chỉ cho phép admin thực hiện
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        List<ReviewResponse> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Admin có thể xem tất cả reviews của một tour
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<ReviewResponse>> getTourReviewsForAdmin(@PathVariable Integer tourId) {
        List<ReviewResponse> reviews = reviewService.getTourReviews(tourId);
        return ResponseEntity.ok(reviews);
    }

    // Admin có thể xem chi tiết một review
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewByIdForAdmin(@PathVariable Integer reviewId) {
        try {
            ReviewResponse review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}