package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
                ViewTourImageMapper.class,
                ViewTourItineraryMapper.class,
                ViewTourScheduleMapper.class,
                ViewCategoryMapper.class
})
public interface ViewTourMapper {
        ViewTourResponse toDto(Tour tour);

        List<ViewTourResponse> toDtoList(List<Tour> tours);
}
