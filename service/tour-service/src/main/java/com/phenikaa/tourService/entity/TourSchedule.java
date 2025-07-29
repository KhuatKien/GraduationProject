package com.phenikaa.tourService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private LocalDate departureDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "departure_time", nullable = false)
    private Double specialPrice; // giá đặc biệt cho lịch trình này

    @Column(name = "available_slot", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status; // AVAILABLE, FULL, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

//    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
//    private List<Booking> bookings;
}
