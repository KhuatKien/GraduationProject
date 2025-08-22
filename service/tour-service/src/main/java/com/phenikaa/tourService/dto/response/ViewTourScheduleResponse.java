package com.phenikaa.tourService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewTourScheduleResponse {
    private Integer scheduleId;
    private Instant departureDate;
    private Instant returnDate;
    private Double specialPrice;
    private Integer availableSlots;
    private String status;
}
