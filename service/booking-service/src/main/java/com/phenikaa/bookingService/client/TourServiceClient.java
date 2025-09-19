package com.phenikaa.bookingService.client;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.dto.response.ScheduleInfoResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "api-gateway", contextId = "tourServiceClient", path = "/tour-service", configuration = FeignTokenInterceptor.class)
public interface TourServiceClient {
    @GetMapping("/api/tour/user/getInfoTour/{scheduleId}")
    GetInfoTour getInfoTour(@PathVariable("scheduleId") Integer scheduleId);

    @GetMapping("/api/tour/user/getSchedule/{scheduleId}")
    ScheduleInfoResponse getSchedule(@PathVariable("scheduleId") Integer scheduleId);

    @PutMapping("/api/tour/user/updateAvailableSlot/{scheduleId}")
    void updateSchedule(@PathVariable("scheduleId") Integer scheduleId,
            @RequestParam("availableSlots") Integer availableSlots);

    @GetMapping("/api/tour/user/getAllSchedules/{tourId}")
    List<ScheduleInfoResponse> getAllSchedules(@PathVariable("tourId") Integer tourId);
}
