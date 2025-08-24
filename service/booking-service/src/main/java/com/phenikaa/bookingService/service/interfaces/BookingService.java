package com.phenikaa.bookingService.service.interfaces;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.entity.Booking;

public interface BookingService {
    Booking createBooking(Integer userId, Integer scheduleId, CreateBookingRequest dto);
}
