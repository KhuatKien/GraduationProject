package com.phenikaa.bookingService.service.interfaces;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.dto.response.AdminBookingResponse;
import com.phenikaa.bookingService.dto.response.BookingStatsResponse;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    Booking createBooking(Integer userId, Integer scheduleId, CreateBookingRequest dto);

    Page<AdminBookingResponse> getAllBookings(Pageable pageable);

    Page<AdminBookingResponse> getAllBookings(Pageable pageable, String search, String status);

    void deleteBooking(Integer bookingId);

    Booking updateBookingStatus(Integer bookingId, BookingStatus status);

    Page<ViewBookingResponse> getUserBookings(Integer userId, Pageable pageable);

    ViewBookingResponse getUserBookingDetail(Integer userId, Integer bookingId);

    Booking cancelUserBooking(Integer userId, Integer bookingId);

    // Thống kê bookings
    BookingStatsResponse getBookingStats();

    // Lấy số lần đặt tour cho một tour cụ thể
    Long getTourBookedCount(Integer tourId);
}
