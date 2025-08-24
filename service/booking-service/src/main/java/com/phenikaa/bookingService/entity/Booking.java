package com.phenikaa.bookingService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id", nullable = false, updatable = false)
    private Integer bookingId;

    @Column(name = "booking_code", unique = true, nullable = false)
    private String bookingCode;

    // Foreign key references to other services
    @Column(name = "user_id", nullable = false)
    private Integer userId; // Reference to User Service

    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId; // Reference to Tour Service

    @Column(name = "adult_count", nullable = false)
    private Integer adultCount;

    @Column(name = "child_count", nullable = false)
    private Integer childCount;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
