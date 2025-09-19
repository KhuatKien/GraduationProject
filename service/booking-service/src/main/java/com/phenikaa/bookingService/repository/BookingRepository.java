package com.phenikaa.bookingService.repository;

import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
        Page<Booking> findByUserId(Integer userId, Pageable pageable);

        Booking findByBookingIdAndUserId(Integer bookingId, Integer userId);

        // Count methods for statistics
        Long countByStatus(BookingStatus status);

        // Search and filter methods
        @Query("SELECT b FROM Booking b WHERE " +
                        "(:search IS NULL OR " +
                        "LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "CAST(b.userId AS string) LIKE CONCAT('%', :search, '%') OR " +
                        "CAST(b.scheduleId AS string) LIKE CONCAT('%', :search, '%')) AND " +
                        "(:status IS NULL OR b.status = :status)")
        Page<Booking> findBySearchAndStatus(@Param("search") String search,
                        @Param("status") BookingStatus status,
                        Pageable pageable);

        // Lấy số lần đặt tour cho một tour cụ thể theo danh sách schedule IDs
        @Query("SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.scheduleId IN :scheduleIds " +
                        "AND (b.status = 'CONFIRMED' OR b.status = 'COMPLETED')")
        Long countByScheduleIdsAndValidStatus(@Param("scheduleIds") java.util.List<Integer> scheduleIds);
}
