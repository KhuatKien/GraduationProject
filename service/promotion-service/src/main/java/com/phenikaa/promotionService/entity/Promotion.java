package com.phenikaa.promotionService.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.phenikaa.promotionService.enums.DiscountType;
import com.phenikaa.promotionService.enums.PromotionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
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
    @NotBlank(message = "Promotion code cannot be blank")
    @Size(min = 3, max = 20, message = "Promotion code must be between 3 and 20 characters")
    private String promotionCode;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Column(columnDefinition = "NVARCHAR(Max)")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    @NotNull(message = "Discount type is required")
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double discountValue;

    @Column(name = "max_discount_amount")
    @PositiveOrZero(message = "Max discount amount must be positive or zero")
    private Double maxDiscountAmount;

    @Column(name = "min_order_amount")
    @PositiveOrZero(message = "Min order amount must be positive or zero")
    private Double minOrderAmount;

    @Column(name = "total_usage_limit", nullable = false)
    @Min(value = 1, message = "Total usage limit must be at least 1")
    private Integer totalUsageLimit;

    @Column(name = "used_count", nullable = false)
    @Min(value = 0, message = "Used count cannot be negative")
    private Integer usedCount;

    @Column(name = "user_usage_limit", nullable = false)
    private Integer userUsageLimit = 1; // giới hạn sử dụng per user (mặc định 1 lần)

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status; // ACTIVE, INACTIVE, EXPIRED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PromotionUsage> usages;
}