package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.client.UserServiceClient;
import com.phenikaa.tourService.dto.request.CreateReviewRequest;
import com.phenikaa.tourService.dto.request.UpdateReviewRequest;
import com.phenikaa.tourService.dto.response.ReviewResponse;
import com.phenikaa.tourService.dto.response.TourReviewSummary;
import com.phenikaa.tourService.dto.response.UserInfoResponse;
import com.phenikaa.tourService.entity.Review;
import com.phenikaa.tourService.repository.ReviewRepository;
import com.phenikaa.tourService.service.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        // Cho phép review nhiều lần - không kiểm tra duplicate
        Review review = Review.builder()
                .userId(request.getUserId())
                .tourId(request.getTourId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    public ReviewResponse updateReview(UpdateReviewRequest request) {
        // Tìm review cần update
        Review existingReview = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review với ID: " + request.getReviewId()));

        // Cập nhật thông tin review
        existingReview.setRating(request.getRating());
        existingReview.setComment(request.getComment());

        Review savedReview = reviewRepository.save(existingReview);
        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Integer userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getTourReviews(Integer tourId) {
        List<Review> reviews = reviewRepository.findByTourId(tourId);
        return reviews.stream()
                .map(review -> {
                    try {
                        // Gọi UserService để lấy thông tin user thật
                        UserInfoResponse userInfo = userServiceClient.getUserInfo(review.getUserId());

                        String userName = userInfo.getFullName() != null ? userInfo.getFullName()
                                : userInfo.getUserName() != null ? userInfo.getUserName()
                                        : "User " + review.getUserId();

                        String userAvatar = userInfo.getAvatar() != null ? userInfo.getAvatar()
                                : "https://ui-avatars.com/api/?name=" + userName + "&background=random";

                        String userEmail = userInfo.getEmail() != null ? userInfo.getEmail()
                                : "user" + review.getUserId() + "@example.com";

                        // Tạo ReviewResponse với thông tin user thật
                        ReviewResponse response = ReviewResponse.fromEntity(review);
                        response.setUserName(userName);
                        response.setUserAvatar(userAvatar);
                        response.setUserEmail(userEmail);

                        return response;

                    } catch (Exception e) {
                        // Fallback về mock data nếu UserService không available
                        System.out.println(
                                "Error getting user info for userId " + review.getUserId() + ": " + e.getMessage());
                        return ReviewResponse.fromEntity(review);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review với ID: " + reviewId));
        return ReviewResponse.fromEntity(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Không tìm thấy review với ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public TourReviewSummary getTourReviewSummary(Integer tourId) {
        Long totalReviews = reviewRepository.countByTourId(tourId);
        Double averageRating = reviewRepository.getAverageRatingByTourId(tourId);

        if (averageRating == null) {
            averageRating = 0.0;
        }

        return new TourReviewSummary(tourId, averageRating, totalReviews);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByTourId(Integer tourId) {
        Double averageRating = reviewRepository.getAverageRatingByTourId(tourId);
        return averageRating != null ? averageRating : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReviewCountByTourId(Integer tourId) {
        return reviewRepository.countByTourId(tourId);
    }
}