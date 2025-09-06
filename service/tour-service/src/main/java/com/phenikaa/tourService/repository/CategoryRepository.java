package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByActiveTrue();

    List<Category> findAllByActive(boolean active);

    Optional<Category> findByName(String name);
}
