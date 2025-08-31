package com.phenikaa.bookingService.service.interfaces;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    Booking createBooking(Integer userId, Integer scheduleId, CreateBookingRequest dto);
    Page<ViewBookingResponse> getAllBookings(Pageable pageable);
    void deleteBooking(Integer bookingId);
}
