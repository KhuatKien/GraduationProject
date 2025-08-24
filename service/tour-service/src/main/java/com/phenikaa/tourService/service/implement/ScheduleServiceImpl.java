package com.phenikaa.tourService.service.implement;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.repository.ScheduleRepository;
import com.phenikaa.tourService.service.interfaces.ScheduleService;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
     private final ScheduleRepository scheduleRepository;
    @Override
    public List<ViewTourScheduleResponse> getAllScheduleByTourId(Integer tourId) {
        return scheduleRepository.findAllByTour_TourId(tourId).stream()
                .map(schedule -> new ViewTourScheduleResponse(
                schedule.getScheduleId(),
                schedule.getDepartureDate(),
                schedule.getReturnDate(),
                schedule.getSpecialPrice(),
                schedule.getAvailableSlots(),
                schedule.getStatus().toString()
        )).collect(Collectors.toList());
    }

    @Override
    public GetInfoTour getInfoTour(Integer scheduleId) {
        Optional<TourSchedule> schedule = scheduleRepository.findById(scheduleId);
        if(schedule.isEmpty()){
            throw new NotFoundException("Schedule not found");
        }
        return new GetInfoTour(
                schedule.get().getAvailableSlots(),
                schedule.get().getTour().getAdultPrice(),
                schedule.get().getTour().getChildPrice());
    }

    @Override
    public void updateSchedule(Integer scheduleId, Integer availableSlots) {
        Optional<TourSchedule> schedule = scheduleRepository.findById(scheduleId);
        if(schedule.isEmpty()){
            throw new NotFoundException("Schedule not found");
        }
        schedule.get().setAvailableSlots(availableSlots);
        scheduleRepository.save(schedule.get());
    }
}

