package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourScheduleRequest {
    private Integer scheduleId;
    private Instant departureDate;
    private Instant returnDate;

    private Integer availableSlots;
    private ScheduleStatus status;
}
