package com.phenikaa.bookingService.repository;

import com.phenikaa.bookingService.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
}
