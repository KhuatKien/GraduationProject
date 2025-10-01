package com.phenikaa.paymentService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Integer paymentId;

    @Column(name = "payment_code", unique = true, nullable = false)
    private String paymentCode;

    @Column(name = "booking_id", nullable = false)
    private Integer bookingId; // Reference to Booking Service

    @Column(name = "user_id", nullable = false)
    private Integer userId; // Reference to User Service

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // MOMO, ZALOPAY, BANK_TRANSFER, CREDIT_CARD, QR_CODE

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "gateway_response")
    private String gatewayResponse;

    @CreationTimestamp
    @Column(name = "paid_at", nullable = false, updatable = false)
    private Instant paidAt;
}
