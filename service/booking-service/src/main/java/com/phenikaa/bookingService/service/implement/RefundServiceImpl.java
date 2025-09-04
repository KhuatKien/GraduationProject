//package com.phenikaa.bookingService.service.implement;
//
//import com.phenikaa.bookingService.dto.request.CreateRefundRequest;
//import com.phenikaa.bookingService.dto.request.ProcessRefundRequest;
//import com.phenikaa.bookingService.dto.response.RefundResponse;
//import com.phenikaa.bookingService.entity.Booking;
//import com.phenikaa.bookingService.entity.Refund;
//import com.phenikaa.bookingService.entity.RefundStatus;
//import com.phenikaa.bookingService.mapper.RefundMapper;
//import com.phenikaa.bookingService.repository.BookingRepository;
//import com.phenikaa.bookingService.repository.RefundRepository;
//import com.phenikaa.bookingService.service.interfaces.RefundService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class RefundServiceImpl implements RefundService {
//
//    private final RefundRepository refundRepository;
//    private final BookingRepository bookingRepository;
//    private final RefundMapper refundMapper;
//
//    @Override
//    public Refund createRefund(CreateRefundRequest request) {
//        // Kiểm tra booking tồn tại
//        Booking booking = bookingRepository.findById(request.getBookingId())
//                .orElseThrow(() -> new EntityNotFoundException("Booking không tìm thấy với ID: " + request.getBookingId()));
//
//        // Kiểm tra xem đã có refund cho booking này chưa
//        if (refundRepository.findByBookingId(request.getBookingId()).isPresent()) {
//            throw new RuntimeException("Booking này đã có yêu cầu hoàn tiền");
//        }
//
//        // Validate refund amount
//        if (request.getRefundAmount() <= 0 || request.getRefundAmount() > booking.getTotalAmount()) {
//            throw new RuntimeException("Số tiền hoàn không hợp lệ");
//        }
//
//        Refund refund = new Refund();
//        refund.setRefundCode("RF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
//        refund.setBookingId(request.getBookingId());
//        refund.setRefundAmount(request.getRefundAmount());
//        refund.setReason(request.getReason());
//        refund.setStatus(RefundStatus.PENDING);
//
//        return refundRepository.save(refund);
//    }
//
//    @Override
//    public Page<RefundResponse> getAllRefunds(Pageable pageable) {
//        Page<Refund> refunds = refundRepository.findAll(pageable);
//        return refunds.map(refund -> {
//            Booking booking = bookingRepository.findById(refund.getBookingId()).orElse(null);
//            return refundMapper.toDto(refund, booking);
//        });
//    }
//
//    @Override
//    public Page<RefundResponse> getRefundsByStatus(RefundStatus status, Pageable pageable) {
//        Page<Refund> refunds = refundRepository.findByStatus(status, pageable);
//        return refunds.map(refund -> {
//            Booking booking = bookingRepository.findById(refund.getBookingId()).orElse(null);
//            return refundMapper.toDto(refund, booking);
//        });
//    }
//
//    @Override
//    public RefundResponse getRefundDetail(Integer refundId) {
//        Refund refund = refundRepository.findById(refundId)
//                .orElseThrow(() -> new EntityNotFoundException("Refund không tìm thấy với ID: " + refundId));
//
//        Booking booking = bookingRepository.findById(refund.getBookingId()).orElse(null);
//        return refundMapper.toDto(refund, booking);
//    }
//
//    @Override
//    public Refund processRefund(Integer refundId, ProcessRefundRequest request, Integer adminId) {
//        Refund refund = refundRepository.findById(refundId)
//                .orElseThrow(() -> new EntityNotFoundException("Refund không tìm thấy với ID: " + refundId));
//
//        // Chỉ có thể xử lý refund đang PENDING
//        if (refund.getStatus() != RefundStatus.PENDING) {
//            throw new RuntimeException("Chỉ có thể xử lý refund đang chờ duyệt");
//        }
//
//        // Validate trạng thái mới
//        if (request.getStatus() == RefundStatus.PENDING) {
//            throw new RuntimeException("Không thể cập nhật về trạng thái PENDING");
//        }
//
//        refund.setStatus(request.getStatus());
//        refund.setAdminNote(request.getAdminNote());
//        refund.setProcessedBy(adminId);
//        refund.setProcessedAt(Instant.now());
//
//        return refundRepository.save(refund);
//    }
//
//    @Override
//    public RefundResponse getRefundByBookingId(Integer bookingId) {
//        Refund refund = refundRepository.findByBookingId(bookingId)
//                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy refund cho booking ID: " + bookingId));
//
//        Booking booking = bookingRepository.findById(bookingId).orElse(null);
//        return refundMapper.toDto(refund, booking);
//    }
//}
