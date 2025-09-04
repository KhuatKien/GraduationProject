//package com.phenikaa.bookingService.controller;
//
//import com.phenikaa.bookingService.dto.request.CreateRefundRequest;
//import com.phenikaa.bookingService.dto.request.ProcessRefundRequest;
//import com.phenikaa.bookingService.dto.response.RefundResponse;
//import com.phenikaa.bookingService.entity.Refund;
//import com.phenikaa.bookingService.entity.RefundStatus;
//import com.phenikaa.bookingService.service.interfaces.RefundService;
//import com.phenikaa.utils.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/api/booking/admin/refund")
//public class RefundAdminController {
//
//    private final RefundService refundService;
//    private final JwtUtil jwtUtil;
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createRefund(@RequestBody CreateRefundRequest request) {
//        try {
//            Refund refund = refundService.createRefund(request);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Tạo yêu cầu hoàn tiền thành công",
//                    "data", refund));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi tạo yêu cầu hoàn tiền: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/getAll")
//    public ResponseEntity<?> getAllRefunds(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        try {
//            Pageable pageable = PageRequest.of(page - 1, size);
//            Page<RefundResponse> refunds = refundService.getAllRefunds(pageable);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Lấy danh sách hoàn tiền thành công",
//                    "data", refunds));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi lấy danh sách hoàn tiền: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/getByStatus/{status}")
//    public ResponseEntity<?> getRefundsByStatus(
//            @PathVariable("status") String status,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        try {
//            RefundStatus refundStatus = RefundStatus.valueOf(status.toUpperCase());
//            Pageable pageable = PageRequest.of(page - 1, size);
//            Page<RefundResponse> refunds = refundService.getRefundsByStatus(refundStatus, pageable);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Lấy danh sách hoàn tiền theo trạng thái thành công",
//                    "data", refunds));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Trạng thái không hợp lệ: " + status));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi lấy danh sách hoàn tiền: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/getDetail/{refundId}")
//    public ResponseEntity<?> getRefundDetail(@PathVariable("refundId") Integer refundId) {
//        try {
//            RefundResponse refund = refundService.getRefundDetail(refundId);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Lấy chi tiết hoàn tiền thành công",
//                    "data", refund));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi lấy chi tiết hoàn tiền: " + e.getMessage()));
//        }
//    }
//
//    @PutMapping("/process/{refundId}")
//    public ResponseEntity<?> processRefund(
//            @PathVariable("refundId") Integer refundId,
//            @RequestBody ProcessRefundRequest request,
//            @RequestHeader("Authorization") String token) {
//        try {
//            Integer adminId = jwtUtil.extractUserId(token);
//            Refund processedRefund = refundService.processRefund(refundId, request, adminId);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Xử lý hoàn tiền thành công",
//                    "data", Map.of(
//                            "refundId", processedRefund.getRefundId(),
//                            "refundCode", processedRefund.getRefundCode(),
//                            "status", processedRefund.getStatus(),
//                            "adminNote", processedRefund.getAdminNote(),
//                            "processedAt", processedRefund.getProcessedAt()
//                    )));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi xử lý hoàn tiền: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/getByBooking/{bookingId}")
//    public ResponseEntity<?> getRefundByBookingId(@PathVariable("bookingId") Integer bookingId) {
//        try {
//            RefundResponse refund = refundService.getRefundByBookingId(bookingId);
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Lấy thông tin hoàn tiền theo booking thành công",
//                    "data", refund));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Lỗi khi lấy thông tin hoàn tiền: " + e.getMessage()));
//        }
//    }
//}
