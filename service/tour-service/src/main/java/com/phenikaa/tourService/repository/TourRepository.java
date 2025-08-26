package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourStatus;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer>, JpaSpecificationExecutor<Tour> {
    @Query("SELECT t FROM Tour t WHERE " +
            "(:filterBy = 'title' AND UPPER(CAST(t.title AS string)) LIKE UPPER(CONCAT('%', :keyword, '%'))) OR " +
            "(:filterBy = 'description' AND UPPER(CAST(t.description AS string)) LIKE UPPER(CONCAT('%', :keyword, '%')))")
    List<Tour> searchByKeywordAndFilter(
            @Param("keyword") String keyword,
            @Param("filterBy") String filterBy);

    @Query("SELECT t FROM Tour t WHERE " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:filterBy IS NULL OR t.status = :filterBy)")
    Page<Tour> searchByKeywordAndFilterWithPagination(@Param("keyword") String keyword,
                                                      @Param("filterBy") String filterBy,
                                                      Pageable pageable);

    // QBE method sử dụng SearchTourCriteria
    @Query("SELECT t FROM Tour t WHERE " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:departure IS NULL OR LOWER(t.departure) LIKE LOWER(CONCAT('%', :departure, '%'))) AND " +
            "(:destination IS NULL OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:categoryName IS NULL OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND " +
            "(:minPrice IS NULL OR t.adultPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.adultPrice <= :maxPrice) AND " +
            "(:minDuration IS NULL OR t.duration >= :minDuration) AND " +
            "(:maxDuration IS NULL OR t.duration <= :maxDuration) AND " +
            "(:featured IS NULL OR t.featured = :featured) AND " +
            "(:isHot IS NULL OR t.isHot = :isHot) AND " +
            "(:hasPromotion IS NULL OR t.hasPromotion = :hasPromotion)")
    Page<Tour> findByQbeCriteria(
            @Param("title") String title,
            @Param("departure") String departure,
            @Param("destination") String destination,
            @Param("status") TourStatus status,
            @Param("categoryName") String categoryName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            @Param("featured") Boolean featured,
            @Param("isHot") Boolean isHot,
            @Param("hasPromotion") Boolean hasPromotion,
            Pageable pageable);
}
