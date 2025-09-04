package com.phenikaa.bookingService.repository;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Page<Booking> findByUserId(Integer userId, Pageable pageable);
    Booking findByBookingIdAndUserId(Integer bookingId, Integer userId);
}
