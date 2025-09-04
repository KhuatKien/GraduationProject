package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.entity.TourStatus;
import com.phenikaa.tourService.projection.TourSummaryProjection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer>, JpaSpecificationExecutor<Tour> {
        @Query("SELECT t FROM Tour t WHERE " +
                        "(:filterBy = 'title' AND UPPER(CAST(t.title AS string)) LIKE UPPER(CONCAT('%', :keyword, '%'))) OR "
                        +
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
                        "(:destination IS NULL OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) AND "
                        +
                        "(:status IS NULL OR t.status = :status) AND " +
                        "(:categoryName IS NULL OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND "
                        +
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

        @Query("SELECT t.tourId as tourId, t.title as title, t.destination as destination, " +
                        "t.departure as departure, t.adultPrice as adultPrice, t.childPrice as childPrice, " +
                        "t.duration as duration, t.status as status, t.featured as featured, " +
                        "t.isHot as isHot, t.hasPromotion as hasPromotion, " +
                        "c.categoryId as categoryId, c.name as categoryName, " +
                        "ti.imageUrl as imageUrl, ti.caption as altText " +
                        "FROM Tour t " +
                        "LEFT JOIN t.category c " +
                        "LEFT JOIN t.images ti ON ti.sortOrder = 1 " +
                        "WHERE t.status = 'ACTIVE'")
        Page<TourSummaryProjection> findAllActiveToursSummary(Pageable pageable);

        // TIME-BASED PAGING METHOD

        @Query("SELECT t.tourId as tourId, t.title as title, t.destination as destination, " +
                        "t.departure as departure, t.adultPrice as adultPrice, t.childPrice as childPrice, " +
                        "t.duration as duration, t.status as status, t.featured as featured, " +
                        "t.isHot as isHot, t.hasPromotion as hasPromotion, " +
                        "c.categoryId as categoryId, c.name as categoryName, " +
                        "ti.imageUrl as imageUrl, ti.caption as altText " +
                        "FROM Tour t " +
                        "LEFT JOIN t.category c " +
                        "LEFT JOIN t.images ti ON ti.sortOrder = 1 " +
                        "WHERE t.status = 'ACTIVE' " +
                        "AND t.createdAt >= :startDate " +
                        "AND t.createdAt < :endDate " +
                        "ORDER BY t.createdAt DESC")
        List<TourSummaryProjection> findAllActiveToursInTimeRange(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);
}
