package com.phenikaa.bookingService.controller;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
import com.phenikaa.bookingService.entity.Booking;
import com.phenikaa.bookingService.service.interfaces.BookingService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/booking/user")
public class BookingUserController {
    private final JwtUtil jwtUtil;
    private final BookingService bookingService;

    @PostMapping("/createBooking/{scheduleId}")
    public ResponseEntity<?> createBooking(
            @PathVariable("scheduleId") Integer scheduleId,
            @RequestHeader("Authorization") String token,
            @RequestBody CreateBookingRequest request){
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Booking booking = bookingService.createBooking(userId, scheduleId, request);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi đặt tour: " + e.getMessage()));
        }
    }
}
