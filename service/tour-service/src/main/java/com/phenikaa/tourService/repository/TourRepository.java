package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Tour;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    @Query("SELECT t FROM Tour t WHERE " +
            "(:filterBy = 'title' AND UPPER(CAST(t.title AS string)) LIKE UPPER(CONCAT('%', :keyword, '%'))) OR " +
            "(:filterBy = 'description' AND UPPER(CAST(t.description AS string)) LIKE UPPER(CONCAT('%', :keyword, '%')))")
    List<Tour> searchByKeywordAndFilter(
            @Param("keyword") String keyword,
            @Param("filterBy") String filterBy);
}
