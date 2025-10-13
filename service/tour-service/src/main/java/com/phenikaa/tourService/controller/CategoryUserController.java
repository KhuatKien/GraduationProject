package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.response.ViewCategoryResponse;
import com.phenikaa.tourService.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category/user")
public class CategoryUserController {
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<ViewCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }
}
