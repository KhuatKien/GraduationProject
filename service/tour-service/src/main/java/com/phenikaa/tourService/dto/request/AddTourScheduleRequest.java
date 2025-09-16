package com.phenikaa.tourService.dto.request;

import com.phenikaa.tourService.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTourScheduleRequest {
    @NotNull(message = "Departure date is required")
    @Future(message = "Departure date must be in the future")
    private Instant departureDate;

    @NotNull(message = "Return date is required")
    @Future(message = "Return date must be in the future")
    private Instant returnDate;

    @NotNull(message = "Available slots is required")
    private Integer availableSlots;

    private ScheduleStatus status;
}
