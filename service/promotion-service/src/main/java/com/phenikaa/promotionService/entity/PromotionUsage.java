package com.phenikaa.promotionService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_usages")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PromotionUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double discountAmount;

    @CreationTimestamp
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "booking_id")
    private Integer bookingId;
}
