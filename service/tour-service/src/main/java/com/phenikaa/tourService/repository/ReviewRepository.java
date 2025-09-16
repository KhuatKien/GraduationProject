package com.phenikaa.tourService.repository;

import com.phenikaa.tourService.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // Tìm review theo user ID
    List<Review> findByUserId(Integer userId);

    // Tìm review theo tour ID
    List<Review> findByTourId(Integer tourId);

    // Tìm review theo user ID và tour ID
    List<Review> findByUserIdAndTourId(Integer userId, Integer tourId);

    // Đếm số review theo tour ID
    long countByTourId(Integer tourId);

    // Tính điểm rating trung bình theo tour ID
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tourId = :tourId")
    Double getAverageRatingByTourId(@Param("tourId") Integer tourId);
}
