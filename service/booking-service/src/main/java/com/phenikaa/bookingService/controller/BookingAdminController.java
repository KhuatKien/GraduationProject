package com.phenikaa.bookingService.controller;

import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
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
    public ResponseEntity<Page<ViewBookingResponse>> getAllBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ViewBookingResponse> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/deleteBooking/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable("id") Integer id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Delete booking successfully");
    }
}
