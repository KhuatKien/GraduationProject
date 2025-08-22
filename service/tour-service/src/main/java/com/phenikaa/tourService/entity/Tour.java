package com.phenikaa.tourService.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tours")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id", nullable = false, updatable = false)
    private Integer tourId;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(Max)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(Max)")
    private String highlights;

    @Column(name = "adult_price", nullable = false)
    private Double adultPrice;

    @Column(name = "child_price", nullable = false)
    private Double childPrice;

    private Integer duration; // số ngày

    @Column(columnDefinition = "NVARCHAR(255)")
    private String departure; // điểm khởi hành

    @Column(columnDefinition = "NVARCHAR(255)")
    private String destination; // điểm đến

    @Enumerated(EnumType.STRING)
    private TourStatus status; // ACTIVE, INACTIVE, FULL, CANCELLED

    private Boolean featured; // tour nổi bật

    @Column(name = "is_hot")
    private Boolean isHot; // tour hot

    @Column(name = "has_promotion")
    private Boolean hasPromotion; // có khuyến mãi

    @Column(columnDefinition = "NVARCHAR(255)")
    private String includes; // bao gồm dịch vụ gì

    @Column(columnDefinition = "NVARCHAR(255)")
    private String excludes; // không bao gồm dịch vụ gì

    @Column(columnDefinition = "NVARCHAR(255)")
    private String terms; // điều kiện

    @Column(name = "create_by", nullable = false)
    private Integer createBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TourImage> images;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TourItinerary> itineraries;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TourSchedule> schedules;
}
