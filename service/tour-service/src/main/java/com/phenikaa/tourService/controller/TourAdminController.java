package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.AddTourRequest;
import com.phenikaa.tourService.dto.request.UpdateTourRequest;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.service.interfaces.TourService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
// @CrossOrigin(origins = "*")
@RequestMapping("/api/tour/admin")
public class TourAdminController {
    private final TourService tourService;
    private final JwtUtil jwtUtil;

    @GetMapping("/getAllTours")
    public ResponseEntity<List<ViewTourResponse>> getAllTours() {
        List<ViewTourResponse> tours = tourService.getAllTours();
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ViewTourResponse>> searchTours(
            @RequestParam String keyword,
            @RequestParam String filterBy) {

        List<ViewTourResponse> tours = tourService.searchToursByKeywordAndFilter(keyword, filterBy);
        return ResponseEntity.ok(tours);
    }

    @PostMapping(value = "/addTour", consumes = "multipart/form-data")
    public ResponseEntity<?> addTour(
            @RequestHeader("Authorization") String token,
            @ModelAttribute AddTourRequest tour) {
        try {
            System.out.println("🚀 Received addTour request");
            System.out.println("📝 Tour title: " + tour.getTitle());
            System.out.println("🖼️  Number of images: " + (tour.getImages() != null ? tour.getImages().size() : 0));

            if (tour.getImages() != null) {
                for (int i = 0; i < tour.getImages().size(); i++) {
                    var img = tour.getImages().get(i);
                    System.out.println("Image " + (i + 1) + ":");
                    System.out.println("  - File: "
                            + (img.getImageFile() != null ? img.getImageFile().getOriginalFilename() : "null"));
                    System.out.println("  - Caption: " + img.getCaption());
                    System.out.println("  - Is Primary: " + img.getIsPrimary());
                }
            }

            Integer userId = jwtUtil.extractUserId(token);
            System.out.println("👤 User ID from token: " + userId);

            Tour createdTour = tourService.addTour(userId, tour);

            System.out.println("✅ Tour created successfully with ID: " + createdTour.getTourId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tour created successfully",
                    "tourId", createdTour.getTourId(),
                    "data", createdTour));
        } catch (IOException e) {
            System.err.println("❌ Image upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi upload ảnh: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ General error creating tour: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo tour: " + e.getMessage()));
        }
    }

    @PostMapping("/updateTour")
    public Tour updateTour(@RequestBody UpdateTourRequest tour) {
        return tourService.updateTour(tour);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViewTourResponse> viewTour(@PathVariable("id") Integer id) {
        ViewTourResponse tour = tourService.viewTour(id);
        return ResponseEntity.ok(tour);
    }

    @DeleteMapping("/deleteTour/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable("id") Integer id) {
        tourService.deleteTour(id);
        return ResponseEntity.ok("Tour deleted successfully");
    }

}
