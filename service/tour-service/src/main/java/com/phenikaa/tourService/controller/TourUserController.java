package com.phenikaa.tourService.controller;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
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

    @GetMapping("/getAllTours/paginated")
    public ResponseEntity<Page<ViewTourResponse>> getAllTours(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ViewTourResponse> tours = tourService.getAllTours(pageable);
        return ResponseEntity.ok(tours);
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
}
