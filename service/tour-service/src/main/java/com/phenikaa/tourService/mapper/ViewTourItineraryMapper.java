package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.response.ViewTourItineraryResponse;
import com.phenikaa.tourService.entity.TourItinerary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewTourItineraryMapper {
    ViewTourItineraryResponse toDto(TourItinerary itinerary);

    List<ViewTourItineraryResponse> toDtoList(List<TourItinerary> itineraries);
}
