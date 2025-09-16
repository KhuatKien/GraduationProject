package com.phenikaa.clusteringService.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phenikaa.clusteringService.dto.UserProfileDTO;
import com.phenikaa.clusteringService.entity.Cluster;
import com.phenikaa.clusteringService.entity.UserProfile;
import com.phenikaa.clusteringService.repository.ClusterRepository;
import com.phenikaa.clusteringService.repository.UserProfileRepository;
import com.phenikaa.clusteringService.service.ClusteringAlgorithm;
import com.phenikaa.clusteringService.service.ClusteringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusteringServiceImpl implements ClusteringService {
    
    private final UserProfileRepository userProfileRepository;
    private final ClusterRepository clusterRepository;
    private final ClusteringAlgorithm clusteringAlgorithm;
    private final ObjectMapper objectMapper;
    
    @Value("${clustering.algorithm.k-means.clusters:5}")
    private int numberOfClusters;
    
    @Override
    @Transactional
    public Map<Integer, List<UserProfile>> performClustering() {
        log.info("Starting clustering process...");
        
        // Get all user profiles
        List<UserProfile> profiles = userProfileRepository.findAll();
        if (profiles.isEmpty()) {
            log.warn("No user profiles found for clustering");
            return new HashMap<>();
        }
        
        log.info("Found {} user profiles for clustering", profiles.size());
        
        // Perform clustering
        Map<Integer, List<UserProfile>> clusters = clusteringAlgorithm.cluster(profiles);
        
        // Update user profiles with cluster assignments
        for (Map.Entry<Integer, List<UserProfile>> entry : clusters.entrySet()) {
            Integer clusterId = entry.getKey();
            List<UserProfile> clusterProfiles = entry.getValue();
            
            // Create or update cluster entity
            Cluster cluster = clusterRepository.findById(clusterId).orElse(new Cluster());
            cluster.setClusterId(clusterId);
            cluster.setClusterName("Cluster " + clusterId);
            cluster.setSize(clusterProfiles.size());
            cluster.setIsActive(true);
            cluster.setDescription(generateClusterDescription(clusterProfiles));
            cluster.setCharacteristics(generateClusterCharacteristics(clusterProfiles));
            cluster.setCentroid(calculateCentroidJson(clusterProfiles));
            
            clusterRepository.save(cluster);
            
            // Update user profiles
            for (UserProfile profile : clusterProfiles) {
                profile.setClusterId(clusterId);
                profile.setClusterConfidence(calculateConfidence(profile, clusterProfiles));
                userProfileRepository.save(profile);
            }
        }
        
        log.info("Clustering completed. Created {} clusters", clusters.size());
        return clusters;
    }
    
    @Override
    @Transactional
    public UserProfile updateUserCluster(Integer userId) {
        log.info("Updating cluster for user: {}", userId);
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
        
        // Get existing clusters
        Map<Integer, List<UserProfile>> existingClusters = new HashMap<>();
        List<Integer> clusterIds = userProfileRepository.findDistinctClusterIds();
        
        for (Integer clusterId : clusterIds) {
            List<UserProfile> clusterProfiles = userProfileRepository.findByClusterId(clusterId);
            existingClusters.put(clusterId, clusterProfiles);
        }
        
        // Assign user to best cluster
        Integer newClusterId = clusteringAlgorithm.assignToCluster(profile, existingClusters);
        profile.setClusterId(newClusterId);
        profile.setClusterConfidence(0.8); // Default confidence for new assignments
        
        return userProfileRepository.save(profile);
    }
    
    @Override
    public List<Integer> getRecommendations(Integer userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
        
        if (profile.getClusterId() == null) {
            log.warn("User {} is not assigned to any cluster", userId);
            return new ArrayList<>();
        }
        
        // Get similar users in the same cluster
        List<UserProfile> similarUsers = userProfileRepository.findByClusterId(profile.getClusterId())
                .stream()
                .filter(p -> !p.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        // TODO: Implement recommendation logic based on similar users' booking history
        // This would typically involve:
        // 1. Getting booking history of similar users
        // 2. Finding popular tours among them
        // 3. Filtering out tours the user has already booked
        // 4. Ranking by popularity and relevance
        
        log.info("Generated {} recommendations for user {}", similarUsers.size(), userId);
        return new ArrayList<>(); // Placeholder - implement actual recommendation logic
    }
    
    @Override
    public Cluster getClusterInfo(Integer clusterId) {
        return clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));
    }
    
    @Override
    public List<Cluster> getAllClusters() {
        return clusterRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<UserProfile> getUsersInCluster(Integer clusterId) {
        return userProfileRepository.findByClusterId(clusterId);
    }
    
    @Override
    @Transactional
    public UserProfile createOrUpdateUserProfile(UserProfileDTO userProfileDTO) {
        log.info("Creating/updating user profile for user: {}", userProfileDTO.getUserId());
        
        UserProfile profile = userProfileRepository.findByUserId(userProfileDTO.getUserId())
                .orElse(new UserProfile());
        
        // Set basic information
        profile.setUserId(userProfileDTO.getUserId());
        
        // Extract demographic features
        if (userProfileDTO.getDateOfBirth() != null) {
            profile.setAgeGroup(determineAgeGroup(userProfileDTO.getDateOfBirth()));
        }
        
        if (userProfileDTO.getAddress() != null) {
            profile.setRegion(determineRegion(userProfileDTO.getAddress()));
        }
        
        // Extract behavioral features from booking history
        if (userProfileDTO.getBookingHistory() != null && !userProfileDTO.getBookingHistory().isEmpty()) {
            extractBehavioralFeatures(profile, userProfileDTO.getBookingHistory());
        }
        
        // Save profile
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        // Update cluster assignment
        updateUserCluster(userProfileDTO.getUserId());
        
        return savedProfile;
    }
    
    private String determineAgeGroup(LocalDate dateOfBirth) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        
        if (age <= 25) return "YOUNG";
        else if (age <= 40) return "ADULT";
        else if (age <= 55) return "MIDDLE";
        else return "SENIOR";
    }
    
    private String determineRegion(String address) {
        if (address == null) return "UNKNOWN";
        
        String lowerAddress = address.toLowerCase();
        if (lowerAddress.contains("hà nội") || lowerAddress.contains("hanoi") || 
            lowerAddress.contains("hải phòng") || lowerAddress.contains("quảng ninh")) {
            return "NORTH";
        } else if (lowerAddress.contains("đà nẵng") || lowerAddress.contains("danang") ||
                   lowerAddress.contains("huế") || lowerAddress.contains("hue") ||
                   lowerAddress.contains("quảng nam")) {
            return "CENTRAL";
        } else if (lowerAddress.contains("hồ chí minh") || lowerAddress.contains("ho chi minh") ||
                   lowerAddress.contains("cần thơ") || lowerAddress.contains("can tho") ||
                   lowerAddress.contains("đồng nai") || lowerAddress.contains("dong nai")) {
            return "SOUTH";
        }
        
        return "UNKNOWN";
    }
    
    private void extractBehavioralFeatures(UserProfile profile, List<UserProfileDTO.BookingHistoryDTO> bookingHistory) {
        if (bookingHistory.isEmpty()) return;
        
        // Calculate average spending
        double avgSpending = bookingHistory.stream()
                .mapToDouble(booking -> booking.getAmount() != null ? booking.getAmount() : 0.0)
                .average()
                .orElse(0.0);
        profile.setAverageSpending(avgSpending);
        
        // Determine preferred tour type (most frequent)
        Map<String, Long> tourTypeCount = bookingHistory.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getTourType() != null ? booking.getTourType() : "UNKNOWN",
                        Collectors.counting()
                ));
        String preferredTourType = tourTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
        profile.setPreferredTourType(preferredTourType);
        
        // Determine group size preference (most frequent)
        Map<String, Long> groupSizeCount = bookingHistory.stream()
                .collect(Collectors.groupingBy(
                        booking -> determineGroupSizeCategory(booking.getGroupSize()),
                        Collectors.counting()
                ));
        String preferredGroupSize = groupSizeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
        profile.setGroupSizePreference(preferredGroupSize);
        
        // Calculate booking frequency
        int bookingCount = bookingHistory.size();
        if (bookingCount <= 1) profile.setBookingFrequency("LOW");
        else if (bookingCount <= 3) profile.setBookingFrequency("MEDIUM");
        else profile.setBookingFrequency("HIGH");
        
        // Calculate promotion responsiveness
        long promotionBookings = bookingHistory.stream()
                .filter(booking -> booking.getPromotionCode() != null && !booking.getPromotionCode().isEmpty())
                .count();
        double responsiveness = (double) promotionBookings / bookingHistory.size();
        profile.setPromotionResponsiveness(responsiveness);
    }
    
    private String determineGroupSizeCategory(Integer groupSize) {
        if (groupSize == null) return "UNKNOWN";
        if (groupSize == 1) return "SOLO";
        else if (groupSize == 2) return "COUPLE";
        else if (groupSize <= 5) return "SMALL_GROUP";
        else return "LARGE_GROUP";
    }
    
    private String generateClusterDescription(List<UserProfile> profiles) {
        if (profiles.isEmpty()) return "Empty cluster";
        
        // Find most common characteristics
        String mostCommonAgeGroup = findMostCommon(profiles, UserProfile::getAgeGroup);
        String mostCommonRegion = findMostCommon(profiles, UserProfile::getRegion);
        String mostCommonTourType = findMostCommon(profiles, UserProfile::getPreferredTourType);
        
        return String.format("Users aged %s from %s region, preferring %s tours", 
                mostCommonAgeGroup, mostCommonRegion, mostCommonTourType);
    }
    
    private String generateClusterCharacteristics(List<UserProfile> profiles) {
        try {
            Map<String, Object> characteristics = new HashMap<>();
            characteristics.put("averageSpending", profiles.stream()
                    .mapToDouble(p -> p.getAverageSpending() != null ? p.getAverageSpending() : 0.0)
                    .average().orElse(0.0));
            characteristics.put("averagePromotionResponsiveness", profiles.stream()
                    .mapToDouble(p -> p.getPromotionResponsiveness() != null ? p.getPromotionResponsiveness() : 0.0)
                    .average().orElse(0.0));
            characteristics.put("size", profiles.size());
            
            return objectMapper.writeValueAsString(characteristics);
        } catch (JsonProcessingException e) {
            log.error("Error generating cluster characteristics", e);
            return "{}";
        }
    }
    
    private String calculateCentroidJson(List<UserProfile> profiles) {
        // This would calculate the actual centroid vector
        // For now, return a placeholder
        return "[]";
    }
    
    private Double calculateConfidence(UserProfile profile, List<UserProfile> clusterProfiles) {
        // Simple confidence calculation based on how well the profile fits the cluster
        // This is a placeholder - implement actual confidence calculation
        return 0.8;
    }
    
    private String findMostCommon(List<UserProfile> profiles, java.util.function.Function<UserProfile, String> extractor) {
        return profiles.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }
}


