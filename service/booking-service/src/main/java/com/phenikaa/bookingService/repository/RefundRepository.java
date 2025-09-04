//package com.phenikaa.bookingService.repository;
//
//import com.phenikaa.bookingService.entity.Refund;
//import com.phenikaa.bookingService.entity.RefundStatus;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//package com.phenikaa.bookingService.entity;
//import java.util.List;
//import java.util.Optional;
//
//public interface RefundRepository extends JpaRepository<Refund, Integer> {
//    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);
//    Optional<Refund> findByBookingId(Integer bookingId);
//    List<Refund> findByProcessedBy(Integer adminId);
//    Page<Refund> findByStatusIn(List<RefundStatus> statuses, Pageable pageable);
//}
//
//
