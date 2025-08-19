package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.*;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.mapper.*;
import com.phenikaa.tourService.repository.CategoryRepository;
import com.phenikaa.tourService.repository.TourRepository;
import com.phenikaa.tourService.service.interfaces.CloudinaryService;
import com.phenikaa.tourService.service.interfaces.TourService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final CategoryRepository categoryRepository;

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

            for (int i = 0; i < dto.getImages().size(); i++) {
                AddTourImageRequest imageRequest = dto.getImages().get(i);
                if (imageRequest.getImageFile() != null && !imageRequest.getImageFile().isEmpty()) {
                    try {
                        // Upload ảnh lên Cloudinary và cập nhật imageUrl trong DTO
                        String imageUrl = cloudinaryService.uploadImage(imageRequest.getImageFile(), folderName);
                        imageRequest.setImageUrl(imageUrl);

                    } catch (IOException e) {
                        throw new IOException(
                                "Failed to upload image: " + imageRequest.getImageFile().getOriginalFilename(), e);
                    }
                } else {
                    System.out.println("Skipping empty image at index " + i);
                }
            }
        }

        // Sử dụng mapper để tạo tour entity (bao gồm cả images đã có URL)
        Tour tour = addTourMapper.toEntity(dto);
        tour.setCreateBy(userId);
        tour.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + dto.getCategoryId())));

        // Gán quan hệ 2 chiều (nếu cần)
        if (tour.getImages() != null) {
            tour.getImages().forEach(image -> image.setTour(tour));
        }
        if (tour.getItineraries() != null) {
            tour.getItineraries().forEach(itinerary -> itinerary.setTour(tour));
        }
        if (tour.getSchedules() != null) {
            tour.getSchedules().forEach(schedule -> {
                schedule.setTour(tour);
                // Tự động set availableSlots = maxParticipants của tour
                schedule.setAvailableSlots(tour.getMaxParticipants());
            });
        }

        Tour savedTour = tourRepository.save(tour);
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

        try {
            // Xóa từng image trong Cloudinary trước (an toàn hơn)
            if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                for (TourImage image : tour.getImages()) {
                    if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                        try {
                            // Extract public ID từ URL và xóa
                            String publicId = cloudinaryService.extractPublicIdFromUrl(image.getImageUrl());
                            cloudinaryService.deleteImage(publicId);
                            System.out.println("Deleted image from Cloudinary: " + publicId);
                        } catch (Exception e) {
                            System.err.println("Failed to delete individual image: " + image.getImageUrl() + " - " + e.getMessage());
                        }
                    }
                }
            }

            // Xóa folder sau cùng (chỉ để cleanup)
            try {
                String folderName = "tours/" + tour.getTitle().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
                cloudinaryService.deleteFolder(folderName);
                System.out.println("Deleted Cloudinary folder: " + folderName);
            } catch (Exception folderException) {
                System.out.println("Note: Folder deletion skipped (may be already empty): " + folderException.getMessage());
                // Không cần throw exception vì images đã được xóa
            }

        } catch (Exception e) {
            System.err.println("Warning: Failed to delete Cloudinary resources for tour " + tourId + ": " + e.getMessage());
            // Vẫn tiếp tục xóa database ngay cả khi Cloudinary fail
        }

        // Xóa tour từ database (cascade sẽ xóa images, schedules, itineraries)
        tourRepository.delete(tour);
        System.out.println("Deleted tour from database: " + tourId);
    }
}
