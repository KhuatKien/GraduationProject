package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
