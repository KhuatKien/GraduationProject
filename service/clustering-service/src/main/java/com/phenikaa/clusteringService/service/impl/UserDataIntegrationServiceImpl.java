package com.phenikaa.clusteringService.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.clusteringService.dto.UserProfileDTO;
import com.phenikaa.clusteringService.entity.UserProfile;
import com.phenikaa.clusteringService.repository.UserProfileRepository;
import com.phenikaa.clusteringService.service.ClusteringService;
import com.phenikaa.clusteringService.service.UserDataIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataIntegrationServiceImpl implements UserDataIntegrationService {
    
    private final UserProfileRepository userProfileRepository;
    private final ClusteringService clusteringService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users";
    private static final String BOOKING_SERVICE_URL = "http://localhost:8082/api/bookings";
    private static final String TOUR_SERVICE_URL = "http://localhost:8083/api/tours";
    
    @Override
    public UserProfile syncUserData(Integer userId) {
        try {
            log.info("Syncing user data for user: {}", userId);
            
            // Get user data from user service
            UserProfileDTO userProfileDTO = getUserDataFromUserService(userId);
            
            // Get booking history from booking service
            List<UserProfileDTO.BookingHistoryDTO> bookingHistory = getBookingHistoryFromBookingService(userId);
            userProfileDTO.setBookingHistory(bookingHistory);
            
            // Create or update user profile
            UserProfile profile = clusteringService.createOrUpdateUserProfile(userProfileDTO);
            
            log.info("Successfully synced user data for user: {}", userId);
            return profile;
            
        } catch (Exception e) {
            log.error("Error syncing user data for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to sync user data", e);
        }
    }
    
    @Override
    public int syncAllUsersData() {
        try {
            log.info("Starting sync of all users data");
            
            // Get all user IDs from user service
            List<Integer> userIds = getAllUserIdsFromUserService();
            
            int syncedCount = 0;
            for (Integer userId : userIds) {
                try {
                    syncUserData(userId);
                    syncedCount++;
                } catch (Exception e) {
                    log.error("Failed to sync user {}: {}", userId, e.getMessage());
                }
            }
            
            log.info("Completed sync of {} users", syncedCount);
            return syncedCount;
            
        } catch (Exception e) {
            log.error("Error syncing all users data: {}", e.getMessage());
            throw new RuntimeException("Failed to sync all users data", e);
        }
    }
    
    @Override
    public UserProfileDTO getUserProfileWithHistory(Integer userId) {
        try {
            UserProfileDTO userProfileDTO = getUserDataFromUserService(userId);
            List<UserProfileDTO.BookingHistoryDTO> bookingHistory = getBookingHistoryFromBookingService(userId);
            userProfileDTO.setBookingHistory(bookingHistory);
            
            return userProfileDTO;
        } catch (Exception e) {
            log.error("Error getting user profile with history for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user profile with history", e);
        }
    }
    
    @Override
    public void updateProfileOnBooking(Integer userId, Object bookingData) {
        try {
            log.info("Updating profile for user {} after new booking", userId);
            
            // Sync user data to update profile
            syncUserData(userId);
            
            // Update cluster assignment
            clusteringService.updateUserCluster(userId);
            
            log.info("Successfully updated profile for user {} after booking", userId);
            
        } catch (Exception e) {
            log.error("Error updating profile for user {} after booking: {}", userId, e.getMessage());
        }
    }
    
    private UserProfileDTO getUserDataFromUserService(Integer userId) {
        try {
            String url = USER_SERVICE_URL + "/" + userId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData = response.getBody();
                
                UserProfileDTO dto = new UserProfileDTO();
                dto.setUserId(userId);
                dto.setUserName((String) userData.get("userName"));
                dto.setEmail((String) userData.get("email"));
                dto.setFullName((String) userData.get("fullName"));
                dto.setAddress((String) userData.get("address"));
                dto.setAvatar((String) userData.get("avatar"));
                
                // Parse date of birth
                String dateOfBirthStr = (String) userData.get("dateOfBirth");
                if (dateOfBirthStr != null) {
                    dto.setDateOfBirth(LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ISO_LOCAL_DATE));
                }
                
                return dto;
            } else {
                throw new RuntimeException("Failed to get user data from user service");
            }
            
        } catch (Exception e) {
            log.error("Error getting user data from user service: {}", e.getMessage());
            throw new RuntimeException("Failed to get user data from user service", e);
        }
    }
    
    private List<UserProfileDTO.BookingHistoryDTO> getBookingHistoryFromBookingService(Integer userId) {
        try {
            String url = BOOKING_SERVICE_URL + "/user/" + userId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseData = response.getBody();
                List<Map<String, Object>> bookings = (List<Map<String, Object>>) responseData.get("bookings");
                
                List<UserProfileDTO.BookingHistoryDTO> bookingHistory = new ArrayList<>();
                
                if (bookings != null) {
                    for (Map<String, Object> booking : bookings) {
                        UserProfileDTO.BookingHistoryDTO historyDTO = new UserProfileDTO.BookingHistoryDTO();
                        historyDTO.setBookingId((Integer) booking.get("bookingId"));
                        historyDTO.setAmount(((Number) booking.get("finalAmount")).doubleValue());
                        historyDTO.setPromotionCode((String) booking.get("promotionCode"));
                        
                        // Get tour details for each booking
                        Integer scheduleId = (Integer) booking.get("scheduleId");
                        if (scheduleId != null) {
                            Map<String, Object> tourDetails = getTourDetailsFromTourService(scheduleId);
                            if (tourDetails != null) {
                                historyDTO.setTourType((String) tourDetails.get("tourType"));
                                historyDTO.setSeason(determineSeason((String) tourDetails.get("startDate")));
                            }
                        }
                        
                        // Calculate group size
                        Integer adultCount = (Integer) booking.get("adultCount");
                        Integer childCount = (Integer) booking.get("childCount");
                        historyDTO.setGroupSize((adultCount != null ? adultCount : 0) + (childCount != null ? childCount : 0));
                        
                        // Calculate lead time (simplified)
                        historyDTO.setLeadTimeDays(calculateLeadTime(booking));
                        
                        bookingHistory.add(historyDTO);
                    }
                }
                
                return bookingHistory;
            } else {
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error getting booking history from booking service: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private Map<String, Object> getTourDetailsFromTourService(Integer scheduleId) {
        try {
            String url = TOUR_SERVICE_URL + "/schedules/" + scheduleId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
            
        } catch (Exception e) {
            log.error("Error getting tour details from tour service: {}", e.getMessage());
            return null;
        }
    }
    
    private List<Integer> getAllUserIdsFromUserService() {
        try {
            String url = USER_SERVICE_URL;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseData = response.getBody();
                List<Map<String, Object>> users = (List<Map<String, Object>>) responseData.get("users");
                
                List<Integer> userIds = new ArrayList<>();
                if (users != null) {
                    for (Map<String, Object> user : users) {
                        Integer userId = (Integer) user.get("userId");
                        if (userId != null) {
                            userIds.add(userId);
                        }
                    }
                }
                
                return userIds;
            }
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error getting all user IDs from user service: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private String determineSeason(String startDateStr) {
        if (startDateStr == null) return "UNKNOWN";
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            int month = startDate.getMonthValue();
            
            if (month >= 3 && month <= 5) return "SPRING";
            else if (month >= 6 && month <= 8) return "SUMMER";
            else if (month >= 9 && month <= 11) return "AUTUMN";
            else return "WINTER";
            
        } catch (Exception e) {
            log.error("Error determining season for date {}: {}", startDateStr, e.getMessage());
            return "UNKNOWN";
        }
    }
    
    private Integer calculateLeadTime(Map<String, Object> booking) {
        try {
            // This is a simplified calculation
            // In reality, you'd compare booking date with tour start date
            return 30; // Default to 30 days
        } catch (Exception e) {
            return 30;
        }
    }
}


