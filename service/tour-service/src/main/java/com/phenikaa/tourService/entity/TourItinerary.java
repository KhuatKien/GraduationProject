package com.phenikaa.tourService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_itineraries")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class TourItinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id", nullable = false, updatable = false)
    private Integer itineraryId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String activities;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String meals; // bữa ăn trong ngày

    @Column(columnDefinition = "NVARCHAR(255)")
    private String accommodation; // chỗ ở

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

}
