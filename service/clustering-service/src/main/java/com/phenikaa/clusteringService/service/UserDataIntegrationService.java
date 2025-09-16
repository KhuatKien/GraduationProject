package com.phenikaa.clusteringService.service;

import com.phenikaa.clusteringService.dto.UserProfileDTO;
import com.phenikaa.clusteringService.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserDataIntegrationService {
    
    /**
     * Sync user data from other services
     * @param userId User ID to sync
     * @return Updated user profile
     */
    UserProfile syncUserData(Integer userId);
    
    /**
     * Sync all users data
     * @return Number of users synced
     */
    int syncAllUsersData();
    
    /**
     * Get user profile with booking history
     * @param userId User ID
     * @return User profile with booking history
     */
    UserProfileDTO getUserProfileWithHistory(Integer userId);
    
    /**
     * Update user profile when new booking is made
     * @param userId User ID
     * @param bookingData Booking information
     */
    void updateProfileOnBooking(Integer userId, Object bookingData);
}


