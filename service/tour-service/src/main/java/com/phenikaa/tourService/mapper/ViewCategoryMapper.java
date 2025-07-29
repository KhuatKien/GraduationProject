package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.response.ViewCategoryResponse;
import com.phenikaa.tourService.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ViewCategoryMapper {
    ViewCategoryResponse toDto(Category category);
}