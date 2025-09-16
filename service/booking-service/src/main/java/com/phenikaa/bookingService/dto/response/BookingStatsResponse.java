package com.phenikaa.bookingService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsResponse {
    private Long totalBookings;
    private Long confirmed;
    private Long pending;
    private Long cancelled;
    private Long refunded;
    private Long completed;
}
