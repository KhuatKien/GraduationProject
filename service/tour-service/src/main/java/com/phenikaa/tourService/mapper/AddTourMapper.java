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
        @Mapping(target = "availableSlots", expression = "java(dto.getAvailableSlots() != null ? dto.getAvailableSlots() : dto.getMaxParticipants())")
        Tour toEntity(AddTourRequest dto);
}
