package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourItineraryRequest;
import com.phenikaa.tourService.dto.request.UpdateTourItineraryRequest;
import com.phenikaa.tourService.entity.TourItinerary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UpdateTourItineraryMapper {
    TourItinerary toEntity(AddTourItineraryRequest dto);

    TourItinerary toEntity(UpdateTourItineraryRequest dto);

    List<TourItinerary> toEntityList(List<AddTourItineraryRequest> dtoList);

    @Mapping(target = "itineraryId", ignore = true) // Không update ID
    void updateEntity(UpdateTourItineraryRequest dto, @MappingTarget TourItinerary entity);

    /**
     * Xử lý cập nhật, xóa và thêm mới itineraries
     */
    default void updateItineraryCollection(List<UpdateTourItineraryRequest> dtoList,
            List<TourItinerary> existingItineraries) {
        if (dtoList == null) {
            existingItineraries.clear();
            return;
        }

        // Tạo map các itineraries có ID để dễ tra cứu
        Map<Integer, UpdateTourItineraryRequest> dtoMap = dtoList.stream()
                .filter(dto -> dto.getItineraryId() != null)
                .collect(Collectors.toMap(UpdateTourItineraryRequest::getItineraryId, dto -> dto));

        // Xóa các itineraries không còn trong danh sách update
        existingItineraries.removeIf(
                existing -> existing.getItineraryId() != null && !dtoMap.containsKey(existing.getItineraryId()));

        // Cập nhật các itineraries có ID
        for (TourItinerary existingItinerary : existingItineraries) {
            if (existingItinerary.getItineraryId() != null && dtoMap.containsKey(existingItinerary.getItineraryId())) {
                UpdateTourItineraryRequest dto = dtoMap.get(existingItinerary.getItineraryId());
                updateEntity(dto, existingItinerary);
            }
        }

        // Thêm mới các itineraries không có ID
        List<TourItinerary> newItineraries = dtoList.stream()
                .filter(dto -> dto.getItineraryId() == null)
                .map(this::toEntity)
                .collect(Collectors.toList());

        existingItineraries.addAll(newItineraries);
    }
}
