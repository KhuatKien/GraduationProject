package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourScheduleRequest;
import com.phenikaa.tourService.entity.TourSchedule;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddTourScheduleMapper {
    TourSchedule toEntity(AddTourScheduleRequest dto);

    List<TourSchedule> toEntityList(List<AddTourScheduleRequest> dtoList);
}
