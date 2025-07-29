package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.*;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.entity.TourItinerary;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.mapper.*;
import com.phenikaa.tourService.repository.TourRepository;
import com.phenikaa.tourService.service.interfaces.TourService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final AddTourMapper addTourMapper;
    private final UpdateTourMapper updateTourMapper;
    private final UpdateTourImageMapper updateTourImageMapper;
    private final UpdateTourItineraryMapper updateTourItineraryMapper;
    private final UpdateTourScheduleMapper updateTourScheduleMapper;
    private final ViewTourMapper viewTourMapper;


    @Override
    public List<ViewTourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll();  // Lấy tất cả các tour từ database
        return tours.stream()
                .map(viewTourMapper::toDto)  // Chuyển đổi từ entity sang DTO
                .collect(Collectors.toList());  // Trả về danh sách DTO
    }

    @Override
    public List<ViewTourResponse> searchToursByKeywordAndFilter(String keyword, String filterBy) {
        // Sử dụng phương thức tìm kiếm với filter trong repository
        List<Tour> tours = tourRepository.searchByKeywordAndFilter(keyword, filterBy);
        return tours.stream()
                .map(viewTourMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Tour addTour(Integer userId, AddTourRequest dto) {
        Tour tour = addTourMapper.toEntity(dto);

        tour.setCreateBy(userId);
        // Gán quan hệ 2 chiều (nếu cần)
        if (tour.getImages() != null) {
            tour.getImages().forEach(image -> image.setTour(tour));
        }
        if (tour.getItineraries() != null) {
            tour.getItineraries().forEach(itinerary -> itinerary.setTour(tour));
        }
        if (tour.getSchedules() != null) {
            tour.getSchedules().forEach(schedule -> schedule.setTour(tour));
        }

        return tourRepository.save(tour);
    }

    @Override
    public Tour updateTour(UpdateTourRequest request) {
        Tour existingTour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new RuntimeException("Tour not found"));

        // Sử dụng method mới để cập nhật với xử lý collections thông minh
        updateTourMapper.updateTourWithCollections(request, existingTour,
                updateTourImageMapper,
                updateTourItineraryMapper,
                updateTourScheduleMapper);

        return tourRepository.save(existingTour);
    }


    @Override
    @Transactional(readOnly = true)
    public ViewTourResponse viewTour(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with ID: " + tourId));
        return viewTourMapper.toDto(tour);
    }
 }