package com.phenikaa.tourService.controller;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.tourService.service.interfaces.ScheduleService;
import com.phenikaa.tourService.service.interfaces.TourService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<ViewTourResponse>> getAllTours() {
        List<ViewTourResponse> tours = tourService.getAllTours();
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
    public ResponseEntity<?> updateAvailableSlot(@PathVariable("scheduleId") Integer scheduleId, @RequestParam("availableSlots") Integer availableSlots) {
        scheduleService.updateSchedule(scheduleId, availableSlots);
        return ResponseEntity.ok("Update available slots successfully");
    }
}
