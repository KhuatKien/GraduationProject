package com.phenikaa.promotionService.entity;

import com.phenikaa.promotionService.enums.DiscountType;
import com.phenikaa.promotionService.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotions")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id", nullable = false, updatable = false)
    private Integer promotionId;

    @Column(name = "promotion_code", unique = true, nullable = false)
    private String promotionCode;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "total_usage_limit", nullable = false)
    private int totalUsageLimit;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "user_usage_limit", nullable = false)
    private int userUsageLimit; // giới hạn sử dụng per user

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "and_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status; // ACTIVE, INACTIVE, EXPIRED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    private List<PromotionUsage> usages;

//    @ManyToMany
//    @JoinTable(
//            name = "promotion_tours",
//            joinColumns = @JoinColumn(name = "promotion_id"),
//            inverseJoinColumns = @JoinColumn(name = "tour_id")
//    )
//    private List<Tour> applicableTours;
}