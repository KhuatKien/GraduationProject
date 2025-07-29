package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.response.ViewTourScheduleResponse;
import com.phenikaa.tourService.entity.TourSchedule;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewTourScheduleMapper {
    ViewTourScheduleResponse toDto(TourSchedule schedule);
    List<ViewTourScheduleResponse> toDtoList(List<TourSchedule> schedules);
}
