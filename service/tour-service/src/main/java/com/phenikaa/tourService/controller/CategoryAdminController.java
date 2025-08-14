package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.response.ViewCategoryResponse;
import com.phenikaa.tourService.entity.Category;
import com.phenikaa.tourService.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category/admin")
public class CategoryAdminController {
    private final CategoryService categoryService;


    // Giữ nguyên endpoint cũ: mặc định trả về ACTIVE
    @GetMapping("/getAllCategories")
    public ResponseEntity<List<ViewCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // Tuỳ chọn: truyền ?active=true/false
    @GetMapping("/search")
    public ResponseEntity<List<ViewCategoryResponse>> search(@RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(categoryService.getAllCategoriesByActive(active));
    }
}
