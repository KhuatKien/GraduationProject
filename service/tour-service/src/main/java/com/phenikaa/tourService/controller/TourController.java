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

import java.util.List;

@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@RequestMapping("/api/admin")
public class TourController {
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

    @PostMapping("/addTour")
    public Tour addTour(@RequestHeader("Authorization") String token, @RequestBody AddTourRequest tour) {
        Integer userId = jwtUtil.extractUserId(token);
        return tourService.addTour(userId, tour);
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

}
