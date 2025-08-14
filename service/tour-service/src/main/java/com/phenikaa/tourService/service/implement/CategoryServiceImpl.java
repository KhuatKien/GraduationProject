package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.response.ViewCategoryResponse;
import com.phenikaa.tourService.entity.Category;
import com.phenikaa.tourService.mapper.ViewCategoryMapper;
import com.phenikaa.tourService.repository.CategoryRepository;
import com.phenikaa.tourService.service.interfaces.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ViewCategoryMapper viewCategoryMapper;

    @Override
    public List<ViewCategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findAllByActiveTrue();
        return categories.stream()
                .map(viewCategoryMapper::toDto)
                .toList();
    }

    @Override
    public List<ViewCategoryResponse> getAllCategoriesByActive(boolean active) {
        List<Category> categories = categoryRepository.findAllByActive(active);
        return categories.stream()
                .map(viewCategoryMapper::toDto)
                .toList();
    }
}
