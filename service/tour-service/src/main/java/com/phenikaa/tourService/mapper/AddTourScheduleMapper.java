package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourScheduleRequest;
import com.phenikaa.tourService.entity.TourSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddTourScheduleMapper {
    @Mapping(target = "departureDate", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "returnDate", dateFormat = "yyyy-MM-dd")
    TourSchedule toEntity(AddTourScheduleRequest dto);

    List<TourSchedule> toEntityList(List<AddTourScheduleRequest> dtoList);
}
