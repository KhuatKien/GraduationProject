package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.SearchTourCriteria;
import com.phenikaa.tourService.dto.response.ReviewResponse;
import com.phenikaa.tourService.dto.response.TourReviewSummary;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.service.interfaces.ReviewService;
import com.phenikaa.tourService.service.interfaces.ScheduleService;
import com.phenikaa.tourService.service.interfaces.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/tour/internal")
public class TourInternalController {
    private final TourService tourService;
    private final ScheduleService scheduleService;
    private final ReviewService reviewService;

    @GetMapping("/getAllTours")
    public ResponseEntity<Page<ViewTourResponse>> getAllTours(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ViewTourResponse> tours = tourService.getAllTours(pageable);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/{tourId}")
    public ResponseEntity<ViewTourResponse> viewTour(@PathVariable("tourId") Integer tourId) {
        ViewTourResponse tour = tourService.viewTour(tourId);
        return ResponseEntity.ok(tour);
    }

    @PostMapping("/search/dynamic")
    public ResponseEntity<Page<ViewTourResponse>> searchToursDynamic(
            @RequestBody SearchTourCriteria criteria,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ViewTourResponse> tours = tourService.searchToursBySpecification(criteria, pageable);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/tour/{tourId}/summary")
    public ResponseEntity<TourReviewSummary> getTourReviewSummary(@PathVariable Integer tourId) {
        TourReviewSummary summary = reviewService.getTourReviewSummary(tourId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("review/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Integer userId) {
        List<ReviewResponse> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<ReviewResponse>> getTourReviews(@PathVariable Integer tourId) {
        List<ReviewResponse> reviews = reviewService.getTourReviews(tourId);
        return ResponseEntity.ok(reviews);
    }
}
