package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<TourImage, Integer> {
}
