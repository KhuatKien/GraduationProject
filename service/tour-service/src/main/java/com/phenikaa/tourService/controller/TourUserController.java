package com.phenikaa.tourService.controller;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.request.SearchTourCriteria;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.dto.response.ScheduleInfoResponse;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.projection.TourSummaryProjection;
import com.phenikaa.tourService.service.interfaces.ScheduleService;
import com.phenikaa.tourService.service.interfaces.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/tour/user")
public class TourUserController {
    private final TourService tourService;
    private final ScheduleService scheduleService;

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

    @GetMapping("/getAllSchedules/{tourId}")
    public ResponseEntity<List<ViewTourScheduleResponse>> getAllSchedules(@PathVariable("tourId") Integer tourId) {
        List<ViewTourScheduleResponse> schedules = scheduleService.getAllScheduleByTourId(tourId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/getInfoTour/{scheduleId}")
    public ResponseEntity<GetInfoTour> getInfoTour(@PathVariable("scheduleId") Integer scheduleId) {
        GetInfoTour infoTour = scheduleService.getInfoTour(scheduleId);
        return ResponseEntity.ok(infoTour);
    }

    @GetMapping("/getSchedule/{scheduleId}")
    public ResponseEntity<ScheduleInfoResponse> getSchedule(@PathVariable("scheduleId") Integer scheduleId) {
        TourSchedule schedule = scheduleService.getScheduleById(scheduleId);

        // Convert entity to DTO
        ScheduleInfoResponse response = new ScheduleInfoResponse();
        response.setScheduleId(schedule.getScheduleId());
        response.setDepartureDate(
                schedule.getDepartureDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        response.setReturnDate(
                schedule.getReturnDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        response.setAvailableSlots(schedule.getAvailableSlots());
        response.setStatus(schedule.getStatus().toString());

        if (schedule.getTour() != null) {
            response.setTourTitle(schedule.getTour().getTitle());
            response.setTourDescription(schedule.getTour().getDescription());
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateAvailableSlot/{scheduleId}")
    public ResponseEntity<?> updateAvailableSlot(@PathVariable("scheduleId") Integer scheduleId,
            @RequestParam("availableSlots") Integer availableSlots) {
        scheduleService.updateSchedule(scheduleId, availableSlots);
        return ResponseEntity.ok("Update available slots successfully");
    }

    @GetMapping("/getAllTours/summary")
    public ResponseEntity<Page<TourSummaryProjection>> getAllToursSummary(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "featured") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Tạo sort với priority: featured DESC, createdAt DESC
        Sort sort = Sort.by("featured").descending()
                .and(Sort.by("createdAt").descending());

        // Nếu client muốn sort theo field khác, override sort
        if (!"featured".equals(sortBy)) {
            sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        }

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<TourSummaryProjection> tours = tourService.getAllActiveToursSummary(pageable);

        return ResponseEntity.ok(tours);
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

    @GetMapping("/AiChat")
    public ResponseEntity<String> aiChatRedirect() {
        return ResponseEntity.ok("AI Chat service is available at /api/tour/chat/");
    }

}
