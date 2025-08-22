package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.AddTourRequest;
import com.phenikaa.tourService.dto.request.UpdateTourRequest;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.service.interfaces.TourService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

            Tour createdTour = tourService.addTour(userId, tour);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tour created successfully",
                    "tourId", createdTour.getTourId(),
                    "data", createdTour));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi upload ảnh: " + e.getMessage()));
        } catch (Exception e) {
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

    @PutMapping("/updateTour/{id}")
    public ResponseEntity<?> updateTourWithFiles(
            @PathVariable("id") Integer tourId,
            @RequestHeader("Authorization") String token,
            @ModelAttribute UpdateTourRequest request) {
        try {
            // Extract userId from token (similar to addTour)
            Integer userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Unable to get user information"));
            }

            // Call service updateTourWithFiles
            Tour updatedTour = tourService.updateTourWithFiles(tourId, userId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tour updated successfully",
                    "tourId", updatedTour.getTourId(),
                    "data", updatedTour));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error uploading images: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Error updating tour: " + e.getMessage()));
        }
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
