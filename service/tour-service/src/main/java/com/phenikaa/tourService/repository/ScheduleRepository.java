package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<TourSchedule, Integer> {
    List<TourSchedule> findAllByTour_TourId(Integer tourId);

    // Lấy schedules có status AVAILABLE và FULL cho một tour
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.tourId = :tourId AND s.status IN (com.phenikaa.tourService.enums.ScheduleStatus.AVAILABLE, com.phenikaa.tourService.enums.ScheduleStatus.FULL)")
    List<TourSchedule> findActiveAndFullSchedulesByTourId(@Param("tourId") Integer tourId);

    // Lấy tất cả schedules cần cập nhật status (không phải CANCELLED)
    @Query("SELECT s FROM TourSchedule s WHERE s.status != com.phenikaa.tourService.enums.ScheduleStatus.CANCELLED")
    List<TourSchedule> findSchedulesToUpdateStatus();

    // Cập nhật status của schedule
    @Modifying
    @Query("UPDATE TourSchedule s SET s.status = :status WHERE s.scheduleId = :scheduleId")
    void updateScheduleStatus(@Param("scheduleId") Integer scheduleId, @Param("status") ScheduleStatus status);
}
