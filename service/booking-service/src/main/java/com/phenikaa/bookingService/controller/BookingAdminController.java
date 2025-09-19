package com.phenikaa.bookingService.controller;

import com.phenikaa.bookingService.dto.request.UpdateBookingStatusRequest;
import com.phenikaa.bookingService.dto.response.AdminBookingResponse;
import com.phenikaa.bookingService.dto.response.BookingStatsResponse;
import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.service.interfaces.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/booking/admin")
public class BookingAdminController {
    private final BookingService bookingService;

    @GetMapping("/getAllBookings")
    public ResponseEntity<Page<AdminBookingResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminBookingResponse> bookings = bookingService.getAllBookings(pageable, search, status);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/deleteBooking/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable("id") Integer id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Delete booking successfully");
    }

    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable("id") Integer bookingId,
            @RequestBody UpdateBookingStatusRequest request) {
        try {
            Booking updatedBooking = bookingService.updateBookingStatus(bookingId, request.getStatus());
            return ResponseEntity
                    .ok("Cập nhật trạng thái booking thành công. Trạng thái mới: " + updatedBooking.getStatus());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật trạng thái booking: " + e.getMessage());
        }
    }

    @GetMapping("/getBookingStats")
    public ResponseEntity<BookingStatsResponse> getBookingStats() {
        BookingStatsResponse stats = bookingService.getBookingStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/getTourBookedCount/{tourId}")
    public ResponseEntity<Long> getTourBookedCount(@PathVariable("tourId") Integer tourId) {
        try {
            Long bookedCount = bookingService.getTourBookedCount(tourId);
            return ResponseEntity.ok(bookedCount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0L);
        }
    }
}
