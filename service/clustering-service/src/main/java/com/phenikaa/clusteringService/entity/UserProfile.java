package com.phenikaa.clusteringService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    // Demographic features
    @Column(name = "age_group")
    private String ageGroup; // YOUNG (18-25), ADULT (26-40), MIDDLE (41-55), SENIOR (55+)

    @Column(name = "region")
    private String region; // NORTH, CENTRAL, SOUTH

    @Column(name = "gender")
    private String gender; // MALE, FEMALE, OTHER

    // Behavioral features
    @Column(name = "preferred_tour_type")
    private String preferredTourType; // ADVENTURE, CULTURAL, RELAXATION, FAMILY

    @Column(name = "preferred_season")
    private String preferredSeason; // SPRING, SUMMER, AUTUMN, WINTER

    @Column(name = "group_size_preference")
    private String groupSizePreference; // SOLO, COUPLE, SMALL_GROUP (3-5), LARGE_GROUP (6+)

    @Column(name = "booking_lead_time")
    private String bookingLeadTime; // LAST_MINUTE (0-7 days), SHORT (8-30 days), MEDIUM (31-90 days), LONG (90+ days)

    @Column(name = "average_spending")
    private Double averageSpending;

    // Engagement features
    @Column(name = "booking_frequency")
    private String bookingFrequency; // LOW (0-1/year), MEDIUM (2-3/year), HIGH (4+/year)

    @Column(name = "activity_level")
    private String activityLevel; // LOW, MEDIUM, HIGH

    @Column(name = "promotion_responsiveness")
    private Double promotionResponsiveness; // 0.0 - 1.0

    // Clustering results
    @Column(name = "cluster_id")
    private Integer clusterId;

    @Column(name = "cluster_confidence")
    private Double clusterConfidence; // 0.0 - 1.0

    @Column(name = "profile_vector", columnDefinition = "TEXT")
    private String profileVector; // JSON string of normalized features

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


