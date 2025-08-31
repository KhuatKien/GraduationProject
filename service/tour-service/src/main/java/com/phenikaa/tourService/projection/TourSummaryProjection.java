package com.phenikaa.tourService.projection;

/**
 * Projection cho việc hiển thị danh sách tour ngắn gọn
 * Chỉ lấy những thông tin cần thiết cho việc hiển thị danh sách
 */
public interface TourSummaryProjection {
    Integer getTourId();

    String getTitle();

    String getDestination();

    String getDeparture();

    Double getAdultPrice();

    Double getChildPrice();

    Integer getDuration();

    String getStatus();

    Boolean getFeatured();

    Boolean getIsHot();

    Boolean getHasPromotion();

    // Category fields
    Integer getCategoryId();

    String getCategoryName();

    // Image fields
    String getImageUrl();

    String getAltText();
}
