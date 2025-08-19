package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourRequest;
import com.phenikaa.tourService.entity.Tour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
                AddTourImageMapper.class,
                AddTourItineraryMapper.class,
                AddTourScheduleMapper.class
})
public interface AddTourMapper {
        @Mapping(target = "category", ignore = true)
        @Mapping(target = "tourId", ignore = true)
        @Mapping(target = "createBy", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        Tour toEntity(AddTourRequest dto);
}
