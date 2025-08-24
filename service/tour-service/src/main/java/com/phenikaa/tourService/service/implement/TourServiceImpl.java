package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.*;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.entity.TourItinerary;
import com.phenikaa.tourService.entity.TourSchedule;
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
    private final CategoryRepository categoryRepository;

    @Override
    public List<ViewTourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll();
        return tours.stream().map(viewTourMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ViewTourResponse> searchToursByKeywordAndFilter(String keyword, String filterBy) {
        List<Tour> tours = tourRepository.searchByKeywordAndFilter(keyword, filterBy);
        return tours.stream().map(viewTourMapper::toDto).collect(Collectors.toList());
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
            tour.getSchedules().forEach(schedule -> {
                schedule.setTour(tour);
                // availableSlots now comes from the request, no need to set from
                // maxParticipants
            });
        }

        return tourRepository.save(tour);
    }

    @Override
    public Tour updateTour(UpdateTourRequest request) {
        Tour existingTour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new RuntimeException("Tour not found"));

        updateTourMapper.updateTourWithCollections(request, existingTour,
                updateTourImageMapper, updateTourItineraryMapper, updateTourScheduleMapper);

        return tourRepository.save(existingTour);
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
                existingSchedule.setSpecialPrice(scheduleDto.getSpecialPrice());
                existingSchedule.setStatus(scheduleDto.getStatus());
                updatedSchedules.add(existingSchedule);
                currentSchedules.remove(existingSchedule);
                System.out.println("✅ Updated existing schedule: " + scheduleDto.getDepartureDate() + " -> "
                        + scheduleDto.getReturnDate());

            } else {
                TourSchedule newSchedule = new TourSchedule();
                newSchedule.setDepartureDate(scheduleDto.getDepartureDate());
                newSchedule.setReturnDate(scheduleDto.getReturnDate());
                newSchedule.setSpecialPrice(scheduleDto.getSpecialPrice());
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
}
