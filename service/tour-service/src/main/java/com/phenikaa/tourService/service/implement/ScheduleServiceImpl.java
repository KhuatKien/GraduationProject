package com.phenikaa.tourService.service.implement;

import com.phenikaa.dto.response.GetInfoTour;
import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.enums.ScheduleStatus;
import com.phenikaa.tourService.repository.ScheduleRepository;
import com.phenikaa.tourService.service.interfaces.ScheduleService;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<ViewTourScheduleResponse> getAllScheduleByTourId(Integer tourId) {
        return scheduleRepository.findAllByTour_TourId(tourId).stream()
                .map(schedule -> new ViewTourScheduleResponse(
                        schedule.getScheduleId(),
                        schedule.getDepartureDate(),
                        schedule.getReturnDate(),
                        schedule.getAvailableSlots(),
                        schedule.getStatus().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public GetInfoTour getInfoTour(Integer scheduleId) {
        Optional<TourSchedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isEmpty()) {
            throw new NotFoundException("Schedule not found");
        }
        return new GetInfoTour(
                schedule.get().getAvailableSlots(),
                schedule.get().getTour().getAdultPrice(),
                schedule.get().getTour().getChildPrice(),
                schedule.get().getTour().getCategory() != null ? schedule.get().getTour().getCategory().getName() : null);
    }

    @Override
    public TourSchedule getScheduleById(Integer scheduleId) {
        Optional<TourSchedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isEmpty()) {
            throw new NotFoundException("Schedule not found with id: " + scheduleId);
        }
        return schedule.get();
    }

    @Override
    @Transactional
    public void updateSchedule(Integer scheduleId, Integer availableSlots) {
        Optional<TourSchedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isEmpty()) {
            throw new NotFoundException("Schedule not found");
        }

        TourSchedule scheduleEntity = schedule.get();
        scheduleEntity.setAvailableSlots(availableSlots);

        // Tự động cập nhật status dựa trên available slots và ngày khởi hành
        Instant now = Instant.now();
        ScheduleStatus oldStatus = scheduleEntity.getStatus();
        ScheduleStatus newStatus = determineScheduleStatus(scheduleEntity, now);

        // Chỉ cập nhật status nếu có thay đổi
        if (newStatus != oldStatus) {
            scheduleEntity.setStatus(newStatus);
            log.info("Schedule {} status changed: {} → {} (slots: {})",
                    scheduleId, oldStatus, newStatus, availableSlots);
        } else {
            log.info("Updated schedule {} - slots: {}, status: {} (no change)",
                    scheduleId, availableSlots, newStatus);
        }

        scheduleRepository.save(scheduleEntity);
    }

    @Override
    @Transactional
    public void updateScheduleStatuses() {
        log.info("Starting automatic schedule status update...");

        Instant now = Instant.now();
        List<TourSchedule> schedulesToUpdate = scheduleRepository.findSchedulesToUpdateStatus();

        int expiredCount = 0;
        int fullCount = 0;
        int availableCount = 0;

        for (TourSchedule schedule : schedulesToUpdate) {
            ScheduleStatus newStatus = determineScheduleStatus(schedule, now);

            if (newStatus != schedule.getStatus()) {
                scheduleRepository.updateScheduleStatus(schedule.getScheduleId(), newStatus);

                switch (newStatus) {
                    case EXPIRED:
                        expiredCount++;
                        log.debug("Schedule {} expired (departure: {})",
                                schedule.getScheduleId(), schedule.getDepartureDate());
                        break;
                    case FULL:
                        fullCount++;
                        log.debug("Schedule {} is now full (slots: {})",
                                schedule.getScheduleId(), schedule.getAvailableSlots());
                        break;
                    case AVAILABLE:
                        availableCount++;
                        log.debug("Schedule {} is now available (slots: {})",
                                schedule.getScheduleId(), schedule.getAvailableSlots());
                        break;
                    case CANCELLED:
                        // Không cần xử lý gì cho CANCELLED vì đã được lọc ra
                        break;
                }
            }
        }

        log.info("Schedule status update completed - Expired: {}, Full: {}, Available: {}",
                expiredCount, fullCount, availableCount);
    }

    @Override
    public List<ViewTourScheduleResponse> getActiveAndFullSchedulesByTourId(Integer tourId) {
        return scheduleRepository.findActiveAndFullSchedulesByTourId(tourId).stream()
                .map(schedule -> new ViewTourScheduleResponse(
                        schedule.getScheduleId(),
                        schedule.getDepartureDate(),
                        schedule.getReturnDate(),
                        schedule.getAvailableSlots(),
                        schedule.getStatus().toString()))
                .collect(Collectors.toList());
    }

    /**
     * Xác định status mới của schedule dựa trên ngày khởi hành và số slot còn lại
     * Priority: EXPIRED > FULL > AVAILABLE
     */
    private ScheduleStatus determineScheduleStatus(TourSchedule schedule, Instant now) {
        // 1. Nếu ngày khởi hành đã qua, chuyển thành EXPIRED (ưu tiên cao nhất)
        if (schedule.getDepartureDate().isBefore(now)) {
            return ScheduleStatus.EXPIRED;
        }

        // 2. Nếu không còn slot (availableSlots = 0), chuyển thành FULL ngay lập tức
        if (schedule.getAvailableSlots() <= 0) {
            return ScheduleStatus.FULL;
        }

        // 3. Còn lại là AVAILABLE (còn slot và chưa hết hạn)
        return ScheduleStatus.AVAILABLE;
    }
}
