//package com.phenikaa.bookingService.service.interfaces;
//
//import com.phenikaa.bookingService.dto.request.CreateRefundRequest;
//import com.phenikaa.bookingService.dto.request.ProcessRefundRequest;
//import com.phenikaa.bookingService.dto.response.RefundResponse;
//import com.phenikaa.bookingService.entity.Refund;
//import com.phenikaa.bookingService.entity.RefundStatus;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//public interface RefundService {
//    Refund createRefund(CreateRefundRequest request);
//    Page<RefundResponse> getAllRefunds(Pageable pageable);
//    Page<RefundResponse> getRefundsByStatus(RefundStatus status, Pageable pageable);
//    RefundResponse getRefundDetail(Integer refundId);
//    Refund processRefund(Integer refundId, ProcessRefundRequest request, Integer adminId);
//    RefundResponse getRefundByBookingId(Integer bookingId);
//}
