package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.response.ViewCategoryResponse;

import java.util.List;

public interface CategoryService {
    List<ViewCategoryResponse> getAllActiveCategories();

    List<ViewCategoryResponse> getAllCategoriesByActive(boolean active);
}
