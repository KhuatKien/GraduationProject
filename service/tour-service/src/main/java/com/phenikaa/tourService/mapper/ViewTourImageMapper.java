package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.response.ViewTourImageResponse;
import com.phenikaa.tourService.entity.TourImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewTourImageMapper {
    ViewTourImageResponse toDto(TourImage image);
    List<ViewTourImageResponse> toDtoList(List<TourImage> images);
}
