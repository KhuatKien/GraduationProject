package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.UpdateTourRequest;
import com.phenikaa.tourService.entity.Tour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        UpdateTourImageMapper.class,
        UpdateTourItineraryMapper.class,
        UpdateTourScheduleMapper.class
})
public interface UpdateTourMapper {
    @Mapping(target = "createBy", ignore = true) // giữ nguyên creator
    @Mapping(target = "createdAt", ignore = true) // giữ nguyên thời điểm tạo
    @Mapping(target = "images", ignore = true) // xử lý riêng
    @Mapping(target = "itineraries", ignore = true) // xử lý riêng
    @Mapping(target = "schedules", ignore = true) // xử lý riêng
    void updateTourFromDto(UpdateTourRequest dto, @MappingTarget Tour entity);

    /**
     * Cập nhật tour với xử lý collections một cách thông minh
     */
    default void updateTourWithCollections(UpdateTourRequest dto, Tour entity,
                                           UpdateTourImageMapper imageMapper,
                                           UpdateTourItineraryMapper itineraryMapper,
                                           UpdateTourScheduleMapper scheduleMapper) {
        // Cập nhật các trường cơ bản
        updateTourFromDto(dto, entity);

        // Xử lý images
        if (dto.getImages() != null) {
            if (entity.getImages() == null) {
                entity.setImages(new java.util.ArrayList<>());
            }
            imageMapper.updateImageCollection(dto.getImages(), entity.getImages());
            // Set tour reference cho các image mới
            entity.getImages().forEach(image -> image.setTour(entity));
        }

        // Xử lý itineraries
        if (dto.getItineraries() != null) {
            if (entity.getItineraries() == null) {
                entity.setItineraries(new java.util.ArrayList<>());
            }
            itineraryMapper.updateItineraryCollection(dto.getItineraries(), entity.getItineraries());
            // Set tour reference cho các itinerary mới
            entity.getItineraries().forEach(itinerary -> itinerary.setTour(entity));
        }

        // Xử lý schedules
        if (dto.getSchedules() != null) {
            if (entity.getSchedules() == null) {
                entity.setSchedules(new java.util.ArrayList<>());
            }
            scheduleMapper.updateScheduleCollection(dto.getSchedules(), entity.getSchedules());
            // Set tour reference cho các schedule mới
            entity.getSchedules().forEach(schedule -> schedule.setTour(entity));
        }
    }
}

