package com.phenikaa.bookingService.controller;

import com.phenikaa.bookingService.dto.request.CreateBookingRequest;
//import com.phenikaa.bookingService.dto.request.CreateRefundRequest;
import com.phenikaa.bookingService.dto.response.ViewBookingResponse;
import com.phenikaa.bookingService.entity.Booking;
//import com.phenikaa.bookingService.entity.Refund;
import com.phenikaa.bookingService.service.interfaces.BookingService;
//import com.phenikaa.bookingService.service.interfaces.RefundService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
//    private final RefundService refundService;

    @PostMapping("/createBooking/{scheduleId}")
    public ResponseEntity<?> createBooking(
            @PathVariable("scheduleId") Integer scheduleId,
            @RequestHeader("Authorization") String token,
            @RequestBody CreateBookingRequest request){
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Booking booking = bookingService.createBooking(userId, scheduleId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt tour thành công",
                    "data", booking));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi đặt tour: " + e.getMessage()));
        }
    }

    @GetMapping("/getMyBookings")
    public ResponseEntity<?> getMyBookings(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ViewBookingResponse> bookings = bookingService.getUserBookings(userId, pageable);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách booking thành công",
                    "data", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách booking: " + e.getMessage()));
        }
    }

    @GetMapping("/getBookingDetail/{bookingId}")
    public ResponseEntity<?> getBookingDetail(
            @PathVariable("bookingId") Integer bookingId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            ViewBookingResponse booking = bookingService.getUserBookingDetail(userId, bookingId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy chi tiết booking thành công",
                    "data", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy chi tiết booking: " + e.getMessage()));
        }
    }

    @PutMapping("/cancelBooking/{bookingId}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable("bookingId") Integer bookingId,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);
            Booking cancelledBooking = bookingService.cancelUserBooking(userId, bookingId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Hủy booking thành công",
                    "data", Map.of(
                            "bookingId", cancelledBooking.getBookingId(),
                            "bookingCode", cancelledBooking.getBookingCode(),
                            "status", cancelledBooking.getStatus(),
                            "updatedAt", cancelledBooking.getUpdatedAt()
                    )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi hủy booking: " + e.getMessage()));
        }
    }

//    @PostMapping("/requestRefund")
//    public ResponseEntity<?> requestRefund(
//            @RequestBody CreateRefundRequest request,
//            @RequestHeader("Authorization") String token) {
//        try {
//            // Kiểm tra booking có thuộc về user này không
//            Integer userId = jwtUtil.extractUserId(token);
//            ViewBookingResponse booking = bookingService.getUserBookingDetail(userId, request.getBookingId());
//
//            // Tạo yêu cầu hoàn tiền
//            Refund refund = refundService.createRefund(request);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Gửi yêu cầu hoàn tiền thành công",
//                    "data", refund));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi gửi yêu cầu hoàn tiền: " + e.getMessage()));
//        }
//    }
}
