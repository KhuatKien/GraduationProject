package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourItineraryRequest;
import com.phenikaa.tourService.entity.TourItinerary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddTourItineraryMapper {
    TourItinerary toEntity(AddTourItineraryRequest dto);

    List<TourItinerary> toEntityList(List<AddTourItineraryRequest> dtoList);
}
