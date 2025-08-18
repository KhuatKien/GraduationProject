package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.*;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.entity.TourItinerary;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.mapper.*;
import com.phenikaa.tourService.repository.TourRepository;
import com.phenikaa.tourService.service.interfaces.CloudinaryService;
import com.phenikaa.tourService.service.interfaces.TourService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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
    private final CloudinaryService cloudinaryService;

    @Override
    public List<ViewTourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll(); // Lấy tất cả các tour từ database
        return tours.stream()
                .map(viewTourMapper::toDto) // Chuyển đổi từ entity sang DTO
                .collect(Collectors.toList()); // Trả về danh sách DTO
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
    public Tour addTour(Integer userId, AddTourRequest dto) throws IOException {
        // Xử lý upload ảnh lên Cloudinary trước
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            String folderName = "tours/" + dto.getTitle().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

            System.out
                    .println("📁 Uploading " + dto.getImages().size() + " images to Cloudinary folder: " + folderName);

            for (int i = 0; i < dto.getImages().size(); i++) {
                AddTourImageRequest imageRequest = dto.getImages().get(i);
                if (imageRequest.getImageFile() != null && !imageRequest.getImageFile().isEmpty()) {
                    try {
                        System.out.println("📤 Uploading image " + (i + 1) + ": "
                                + imageRequest.getImageFile().getOriginalFilename());

                        // Upload ảnh lên Cloudinary và cập nhật imageUrl trong DTO
                        String imageUrl = cloudinaryService.uploadImage(imageRequest.getImageFile(), folderName);
                        imageRequest.setImageUrl(imageUrl);

                        System.out.println("✅ Image uploaded successfully: " + imageUrl);
                    } catch (IOException e) {
                        System.err.println("❌ Failed to upload image " + (i + 1) + ": " + e.getMessage());
                        throw new IOException(
                                "Failed to upload image: " + imageRequest.getImageFile().getOriginalFilename(), e);
                    }
                } else {
                    System.out.println("⚠️  Skipping empty image at index " + i);
                }
            }
        }

        // Sử dụng mapper để tạo tour entity (bao gồm cả images đã có URL)
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

        System.out.println("💾 Saving tour to database with " + (tour.getImages() != null ? tour.getImages().size() : 0)
                + " images");
        Tour savedTour = tourRepository.save(tour);
        System.out.println("✅ Tour saved successfully with ID: " + savedTour.getTourId());

        return savedTour;
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

    @Override
    public void deleteTour(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with ID: " + tourId));
        tourRepository.delete(tour);
    }
}
