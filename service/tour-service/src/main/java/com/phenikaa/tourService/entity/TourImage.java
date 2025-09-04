package com.phenikaa.tourService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_images")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class    TourImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_image_id", nullable = false, updatable = false)
    private Integer imageId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "caption", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String caption;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary; // ảnh chính

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;
}