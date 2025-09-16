package com.phenikaa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInfoResponse {
    private Integer scheduleId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer availableSlots;
    private String status;
    private String tourTitle;
    private String tourDescription;
}

