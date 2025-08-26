package com.phenikaa.tourService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tour_schedules")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class TourSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", nullable = false, updatable = false)
    private Integer scheduleId;

    @Column(name = "departure_date", nullable = false)
    private Instant departureDate;

    @Column(name = "return_date", nullable = false)
    private Instant returnDate;

    @Column(name = "available_slot", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status; // AVAILABLE, FULL, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    // @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    // private List<Booking> bookings;
}
