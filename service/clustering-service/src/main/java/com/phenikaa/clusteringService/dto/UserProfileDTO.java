package com.phenikaa.clusteringService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Integer userId;
    private String userName;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private String avatar;
    
    // Booking history
    private List<BookingHistoryDTO> bookingHistory;
    
    // Clustering result
    private Integer clusterId;
    private String clusterName;
    private Double clusterConfidence;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingHistoryDTO {
        private Integer bookingId;
        private String tourType;
        private String season;
        private Integer groupSize;
        private Double amount;
        private Integer leadTimeDays;
        private String promotionCode;
    }
}


