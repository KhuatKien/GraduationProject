//package com.phenikaa.bookingService.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.Instant;
//
//@Entity
//@Table(name = "refunds")
//@NoArgsConstructor
//@AllArgsConstructor
//@Setter
//@Getter
//@ToString
//public class Refund {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "refund_id", nullable = false, updatable = false)
//    private Integer refundId;
//
//    @Column(name = "refund_code", unique = true, nullable = false)
//    private String refundCode;
//
//    @Column(name = "booking_id", nullable = false)
//    private Integer bookingId; // Reference to Booking
//
//    @Column(name = "refund_amount", nullable = false)
//    private Double refundAmount;
//
//    @Column(name = "reason", columnDefinition = "TEXT")
//    private String reason;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false)
//    private RefundStatus status; // PENDING, APPROVED, REJECTED, COMPLETED
//
//    @Column(name = "admin_note", columnDefinition = "TEXT")
//    private String adminNote;
//
//    @Column(name = "processed_by")
//    private Integer processedBy; // Admin user ID
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt;
//
//    @Column(name = "processed_at")
//    private Instant processedAt;
//}
