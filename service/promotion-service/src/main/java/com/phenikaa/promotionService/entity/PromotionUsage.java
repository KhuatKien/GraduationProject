package com.phenikaa.promotionService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_usages", uniqueConstraints = {
        @UniqueConstraint(columnNames = "booking_id", name = "uk_booking_promotion"),
        @UniqueConstraint(columnNames = { "user_id", "promotion_id" }, name = "uk_user_promotion")
})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PromotionUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotNull(message = "Discount amount is required")
    @PositiveOrZero(message = "Discount amount must be positive or zero")
    private Double discountAmount;

    @Column(name = "order_amount", nullable = false)
    @NotNull(message = "Order amount is required")
    @Positive(message = "Order amount must be positive")
    private Double orderAmount;

    @CreationTimestamp
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    @NotNull(message = "Promotion is required")
    private Promotion promotion;

    @Column(name = "user_id")
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;

    @Column(name = "booking_id")
    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be positive")
    private Integer bookingId;
}
