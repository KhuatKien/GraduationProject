package com.phenikaa.tourService.dto.response;

import com.phenikaa.tourService.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Integer reviewId;
    private Integer userId;
    private Integer tourId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    // User information
    private String userName;
    private String userAvatar;
    private String userEmail;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setUserId(review.getUserId());
        response.setTourId(review.getTourId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());

        // Mock user data - trong thực tế sẽ gọi UserService
        response.setUserName("User " + review.getUserId());
        response.setUserAvatar("https://ui-avatars.com/api/?name=User+" + review.getUserId() + "&background=random");
        response.setUserEmail("user" + review.getUserId() + "@example.com");

        return response;
    }
}
