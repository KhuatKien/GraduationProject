package com.phenikaa.bookingService.repository;

import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Page<Booking> findByUserId(Integer userId, Pageable pageable);

    Booking findByBookingIdAndUserId(Integer bookingId, Integer userId);

    // Count methods for statistics
    Long countByStatus(BookingStatus status);
}
