package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.*;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.entity.TourItinerary;
import com.phenikaa.tourService.entity.TourSchedule;
import com.phenikaa.tourService.mapper.*;
import com.phenikaa.tourService.projection.TourSummaryProjection;
import com.phenikaa.tourService.repository.CategoryRepository;
import com.phenikaa.tourService.repository.TourRepository;
import com.phenikaa.tourService.service.interfaces.CloudinaryService;
import com.phenikaa.tourService.service.interfaces.TourService;
import com.phenikaa.tourService.specification.TourSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

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
    public List<ViewTourResponse> searchToursByKeywordAndFilter(String keyword, String filterBy) {
        List<Tour> tours = tourRepository.searchByKeywordAndFilter(keyword, filterBy);
        return tours.stream().map(viewTourMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<ViewTourResponse> getAllTours(Pageable pageable) {
        Page<Tour> tourPage = tourRepository.findAll(pageable);

        // Map tours with additional statistics
        List<ViewTourResponse> toursWithStats = tourPage.getContent().stream()
                .map(tour -> {
                    // Get review statistics
                    Double averageRating = tourRepository.getAverageRatingByTourId(tour.getTourId());
                    Long reviewCount = tourRepository.countByTourId(tour.getTourId());

                    // Map basic tour data
                    ViewTourResponse response = viewTourMapper.toDto(tour);

                    // Set additional statistics
                    response.setAverageRating(averageRating != null ? averageRating : 0.0);
                    response.setReviewCount(reviewCount);

                    return response;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(toursWithStats, pageable, tourPage.getTotalElements());
    }

    @Override
    public Page<ViewTourResponse> searchToursByKeywordAndFilterWithPagination(String keyword, String filterBy,
            Pageable pageable) {
        Page<Tour> tourPage = tourRepository.searchByKeywordAndFilterWithPagination(keyword, filterBy, pageable);
        return tourPage.map(viewTourMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViewTourResponse> searchToursByQbe(SearchTourCriteria criteria, Pageable pageable) {
        Page<Tour> tours = tourRepository.findByQbeCriteria(
                criteria.getTitle(),
                criteria.getDeparture(),
                criteria.getDestination(),
                criteria.getStatus(),
                criteria.getCategoryName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinDuration(),
                criteria.getMaxDuration(),
                criteria.getFeatured(),
                criteria.getIsHot(),
                criteria.getHasPromotion(),
                pageable);
        return tours.map(viewTourMapper::toDto);
    }

    @Override
    public Page<ViewTourResponse> searchToursBySpecification(SearchTourCriteria criteria, Pageable pageable) {
        // Tạo Specification từ criteria
        Specification<Tour> spec = TourSpecification.withDynamicFilters(criteria);

        // Thực hiện search với Specification
        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

        // Convert to DTO
        return tourPage.map(viewTourMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViewTourResponse> searchToursByExample(SearchTourCriteria criteria, Pageable pageable) {
        // Tạo Example object từ criteria
        Tour exampleTour = createExampleTour(criteria);

        // Tạo ExampleMatcher đơn giản với các rule so sánh
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        // Tạo Example object
        Example<Tour> example = Example.of(exampleTour, matcher);

        // Thực hiện search với Example
        Page<Tour> tours = tourRepository.findAll(example, pageable);

        // Convert to DTO
        return tours.map(viewTourMapper::toDto);
    }

    private Tour createExampleTour(SearchTourCriteria criteria) {
        Tour exampleTour = new Tour();
        if (criteria.getTitle() != null && !criteria.getTitle().isEmpty()) {
            exampleTour.setTitle(criteria.getTitle());
        }
        if (criteria.getDeparture() != null && !criteria.getDeparture().isEmpty()) {
            exampleTour.setDeparture(criteria.getDeparture());
        }
        if (criteria.getDestination() != null && !criteria.getDestination().isEmpty()) {
            exampleTour.setDestination(criteria.getDestination());
        }
        if (criteria.getStatus() != null) {
            exampleTour.setStatus(criteria.getStatus());
        }
        if (criteria.getCategoryName() != null && !criteria.getCategoryName().isEmpty()) {
            exampleTour.setCategory(categoryRepository.findByName(criteria.getCategoryName())
                    .orElse(null));
        }
        if (criteria.getMinPrice() != null) {
            exampleTour.setAdultPrice(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            exampleTour.setAdultPrice(criteria.getMaxPrice());
        }
        if (criteria.getMinDuration() != null) {
            exampleTour.setDuration(criteria.getMinDuration());
        }
        if (criteria.getMaxDuration() != null) {
            exampleTour.setDuration(criteria.getMaxDuration());
        }
        if (criteria.getFeatured() != null) {
            exampleTour.setFeatured(criteria.getFeatured());
        }
        if (criteria.getIsHot() != null) {
            exampleTour.setIsHot(criteria.getIsHot());
        }
        if (criteria.getHasPromotion() != null) {
            exampleTour.setHasPromotion(criteria.getHasPromotion());
        }
        return exampleTour;
    }

    @Override
    public Tour addTour(Integer userId, AddTourRequest dto) throws IOException {
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            String folderName = "tours/" + dto.getTitle().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

            for (int i = 0; i < dto.getImages().size(); i++) {
                AddTourImageRequest imageRequest = dto.getImages().get(i);
                if (imageRequest.getImageFile() != null && !imageRequest.getImageFile().isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadImage(imageRequest.getImageFile(), folderName);
                        imageRequest.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        throw new IOException(
                                "Failed to upload image: " + imageRequest.getImageFile().getOriginalFilename(), e);
                    }
                }
            }
        }

        Tour tour = addTourMapper.toEntity(dto);
        tour.setCreateBy(userId);
        tour.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + dto.getCategoryId())));

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
    public Tour updateTourWithFiles(Integer tourId, Integer userId, UpdateTourRequest dto) throws IOException {
        Tour existingTour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with ID: " + tourId));

        existingTour.setTitle(dto.getTitle());
        existingTour.setDescription(dto.getDescription());
        existingTour.setHighlights(dto.getHighlights());
        existingTour.setAdultPrice(dto.getAdultPrice());
        existingTour.setChildPrice(dto.getChildPrice());
        existingTour.setDuration(dto.getDuration());
        existingTour.setDeparture(dto.getDeparture());
        existingTour.setDestination(dto.getDestination());
        existingTour.setStatus(dto.getStatus());
        existingTour.setFeatured(dto.getFeatured());
        existingTour.setIsHot(dto.getIsHot());
        existingTour.setHasPromotion(dto.getHasPromotion());
        existingTour.setIncludes(dto.getIncludes());
        existingTour.setExcludes(dto.getExcludes());
        existingTour.setTerms(dto.getTerms());

        if (dto.getCategoryId() != null) {
            existingTour.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Category not found with ID: " + dto.getCategoryId())));
        }

        updateTourImages(existingTour, dto.getImages());
        updateTourSchedules(existingTour, dto.getSchedules());
        updateTourItineraries(existingTour, dto.getItineraries());

        return tourRepository.save(existingTour);
    }

    private void updateTourImages(Tour existingTour, List<UpdateTourImageRequest> newImageDtos) throws IOException {
        if (newImageDtos == null) {
            newImageDtos = new ArrayList<>();
        }

        String folderName = "tours/" + existingTour.getTitle().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        List<TourImage> currentImages = new ArrayList<>(existingTour.getImages());
        List<TourImage> updatedImages = new ArrayList<>();

        for (UpdateTourImageRequest imageDto : newImageDtos) {
            TourImage imageToKeep = null;

            if (imageDto.getImageId() != null) {
                imageToKeep = currentImages.stream()
                        .filter(img -> imageDto.getImageId().equals(img.getImageId()))
                        .findFirst().orElse(null);
            }

            if (imageToKeep == null && imageDto.getImageUrl() != null && !imageDto.getImageUrl().isEmpty()) {
                imageToKeep = currentImages.stream()
                        .filter(img -> img.getImageUrl().equals(imageDto.getImageUrl()))
                        .findFirst().orElse(null);
            }

            if (imageToKeep != null) {
                imageToKeep.setCaption(imageDto.getCaption() != null ? imageDto.getCaption() : "");
                imageToKeep.setIsPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false);
                imageToKeep.setSortOrder(imageDto.getSortOrder() != null ? imageDto.getSortOrder() : 0);
                updatedImages.add(imageToKeep);
                currentImages.remove(imageToKeep);

            } else if (imageDto.getImageFile() != null && !imageDto.getImageFile().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadImage(imageDto.getImageFile(), folderName);
                    TourImage newImage = TourImage.builder()
                            .imageUrl(imageUrl)
                            .caption(imageDto.getCaption() != null ? imageDto.getCaption() : "")
                            .isPrimary(imageDto.getIsPrimary() != null ? imageDto.getIsPrimary() : false)
                            .sortOrder(imageDto.getSortOrder() != null ? imageDto.getSortOrder() : 0)
                            .tour(existingTour)
                            .build();
                    updatedImages.add(newImage);
                } catch (IOException e) {
                    throw new IOException(
                            "Failed to upload new image: " + imageDto.getImageFile().getOriginalFilename(), e);
                }
            }
        }

        for (TourImage deletedImage : currentImages) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(deletedImage.getImageUrl());
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete image from Cloudinary: " + deletedImage.getImageUrl()
                        + " - " + e.getMessage());
            }
        }

        existingTour.getImages().clear();
        existingTour.getImages().addAll(updatedImages);
    }

    private void updateTourSchedules(Tour existingTour, List<UpdateTourScheduleRequest> newScheduleDtos) {
        if (newScheduleDtos == null) {
            newScheduleDtos = new ArrayList<>();
        }

        List<TourSchedule> currentSchedules = new ArrayList<>(existingTour.getSchedules());
        List<TourSchedule> updatedSchedules = new ArrayList<>();

        for (UpdateTourScheduleRequest scheduleDto : newScheduleDtos) {
            TourSchedule existingSchedule = null;

            if (scheduleDto.getScheduleId() != null) {
                existingSchedule = currentSchedules.stream()
                        .filter(s -> scheduleDto.getScheduleId().equals(s.getScheduleId()))
                        .findFirst().orElse(null);
            }

            if (existingSchedule == null) {
                existingSchedule = currentSchedules.stream()
                        .filter(schedule -> schedule.getDepartureDate().equals(scheduleDto.getDepartureDate())
                                && schedule.getReturnDate().equals(scheduleDto.getReturnDate()))
                        .findFirst().orElse(null);
            }

            if (existingSchedule != null) {
                existingSchedule.setStatus(scheduleDto.getStatus());
                updatedSchedules.add(existingSchedule);
                currentSchedules.remove(existingSchedule);
                System.out.println("✅ Updated existing schedule: " + scheduleDto.getDepartureDate() + " -> "
                        + scheduleDto.getReturnDate());

            } else {
                TourSchedule newSchedule = new TourSchedule();
                newSchedule.setDepartureDate(scheduleDto.getDepartureDate());
                newSchedule.setReturnDate(scheduleDto.getReturnDate());
                newSchedule.setStatus(scheduleDto.getStatus());
                newSchedule.setAvailableSlots(scheduleDto.getAvailableSlots()); // Get from request instead of
                                                                                // maxParticipants
                newSchedule.setTour(existingTour);
                updatedSchedules.add(newSchedule);
            }
        }

        existingTour.getSchedules().clear();
        existingTour.getSchedules().addAll(updatedSchedules);
    }

    private void updateTourItineraries(Tour existingTour, List<UpdateTourItineraryRequest> newItineraryDtos) {
        if (newItineraryDtos == null) {
            newItineraryDtos = new ArrayList<>();
        }

        List<TourItinerary> currentItineraries = new ArrayList<>(existingTour.getItineraries());
        List<TourItinerary> updatedItineraries = new ArrayList<>();

        for (UpdateTourItineraryRequest itineraryDto : newItineraryDtos) {
            TourItinerary existingItinerary = null;

            if (itineraryDto.getItineraryId() != null) {
                existingItinerary = currentItineraries.stream()
                        .filter(i -> itineraryDto.getItineraryId().equals(i.getItineraryId()))
                        .findFirst().orElse(null);
            }

            if (existingItinerary == null) {
                existingItinerary = currentItineraries.stream()
                        .filter(itinerary -> itinerary.getDayNumber().equals(itineraryDto.getDayNumber()))
                        .findFirst().orElse(null);
            }

            if (existingItinerary != null) {
                existingItinerary.setTitle(itineraryDto.getTitle());
                existingItinerary.setDescription(itineraryDto.getDescription());
                existingItinerary.setActivities(itineraryDto.getActivities());
                existingItinerary.setMeals(itineraryDto.getMeals());
                existingItinerary.setAccommodation(itineraryDto.getAccommodation());
                updatedItineraries.add(existingItinerary);
                currentItineraries.remove(existingItinerary);
            } else {
                TourItinerary newItinerary = new TourItinerary();
                newItinerary.setDayNumber(itineraryDto.getDayNumber());
                newItinerary.setTitle(itineraryDto.getTitle());
                newItinerary.setDescription(itineraryDto.getDescription());
                newItinerary.setActivities(itineraryDto.getActivities());
                newItinerary.setMeals(itineraryDto.getMeals());
                newItinerary.setAccommodation(itineraryDto.getAccommodation());
                newItinerary.setTour(existingTour);
                updatedItineraries.add(newItinerary);
            }
        }

        existingTour.getItineraries().clear();
        updatedItineraries.sort((a, b) -> Integer.compare(a.getDayNumber(), b.getDayNumber()));
        existingTour.getItineraries().addAll(updatedItineraries);
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
            if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                for (TourImage image : tour.getImages()) {
                    if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                        try {
                            String publicId = cloudinaryService.extractPublicIdFromUrl(image.getImageUrl());
                            cloudinaryService.deleteImage(publicId);
                            System.out.println("Deleted image from Cloudinary: " + publicId);
                        } catch (Exception e) {
                            System.err.println("Failed to delete individual image: " + image.getImageUrl() + " - "
                                    + e.getMessage());
                        }
                    }
                }
            }

            try {
                String folderName = "tours/" + tour.getTitle().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
                cloudinaryService.deleteFolder(folderName);
            } catch (Exception folderException) {
                System.out.println(
                        "Note: Folder deletion skipped (may be already empty): " + folderException.getMessage());
            }

        } catch (Exception e) {
            System.err.println(
                    "Warning: Failed to delete Cloudinary resources for tour " + tourId + ": " + e.getMessage());
        }

        tourRepository.delete(tour);
    }

    // ========== PROJECTION METHOD ==========

    @Override
    @Transactional(readOnly = true)
    public Page<TourSummaryProjection> getAllActiveToursSummary(Pageable pageable) {
        return tourRepository.findAllActiveToursSummary(pageable);
    }

    // TIME-BASED PAGING METHOD

    @Override
    @Transactional(readOnly = true)
    public Page<TourSummaryProjection> getAllActiveToursTimeBased(int page) {
        Instant now = Instant.now();

        Instant startDate = now.minus(Duration.ofDays(page * 7));
        Instant endDate = now.minus(Duration.ofDays((page - 1) * 7));

        List<TourSummaryProjection> allToursInTimeRange = tourRepository.findAllActiveToursInTimeRange(
                startDate, endDate);

        int totalElements = allToursInTimeRange.size();

        // Nếu không có tour nào, trả về Page rỗng
        if (totalElements == 0) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        }

        // Nếu có tour, tạo Page bình thường
        Pageable pageRequest = PageRequest.of(0, totalElements);
        return new PageImpl<>(allToursInTimeRange, pageRequest, totalElements);
    }
}
