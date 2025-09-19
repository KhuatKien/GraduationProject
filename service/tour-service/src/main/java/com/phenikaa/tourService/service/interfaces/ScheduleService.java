package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.tourService.entity.TourSchedule;

import java.util.List;

public interface ScheduleService {
    List<ViewTourScheduleResponse> getAllScheduleByTourId(Integer tourId);

    GetInfoTour getInfoTour(Integer scheduleId);

    TourSchedule getScheduleById(Integer scheduleId);

    void updateSchedule(Integer scheduleId, Integer availableSlots);

    // Method để tự động cập nhật status schedule
    void updateScheduleStatuses();

    // Method để lấy schedules có status ACTIVE và FULL
    List<ViewTourScheduleResponse> getActiveAndFullSchedulesByTourId(Integer tourId);
}
