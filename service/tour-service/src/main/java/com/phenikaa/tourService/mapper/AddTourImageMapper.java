package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourImageRequest;
import com.phenikaa.tourService.entity.TourImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddTourImageMapper {
    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "tour", ignore = true)
    TourImage toEntity(AddTourImageRequest dto);

    List<TourImage> toEntityList(List<AddTourImageRequest> dtoList);
}
