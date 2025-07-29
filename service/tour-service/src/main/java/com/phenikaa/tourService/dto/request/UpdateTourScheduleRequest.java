package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.entity.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourScheduleRequest {
    private Integer scheduleId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Double specialPrice;
    private Integer availableSlots;
    private ScheduleStatus status;
}
