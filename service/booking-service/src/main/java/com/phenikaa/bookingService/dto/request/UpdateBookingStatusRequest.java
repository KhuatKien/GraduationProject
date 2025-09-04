package com.phenikaa.bookingService.dto.request;

import com.phenikaa.bookingService.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequest {
    private BookingStatus status;
}
