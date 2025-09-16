package com.phenikaa.clusteringService.service;

import com.phenikaa.clusteringService.dto.UserProfileDTO;
import com.phenikaa.clusteringService.entity.Cluster;
import com.phenikaa.clusteringService.entity.UserProfile;

import java.util.List;
import java.util.Map;

public interface ClusteringService {
    
    /**
     * Perform clustering on all user profiles
     * @return Map of cluster ID to list of user profiles
     */
    Map<Integer, List<UserProfile>> performClustering();
    
    /**
     * Update clustering for a specific user
     * @param userId User ID to update
     * @return Updated user profile
     */
    UserProfile updateUserCluster(Integer userId);
    
    /**
     * Get user recommendations based on clustering
     * @param userId User ID
     * @return List of recommended tour IDs
     */
    List<Integer> getRecommendations(Integer userId);
    
    /**
     * Get cluster information
     * @param clusterId Cluster ID
     * @return Cluster information
     */
    Cluster getClusterInfo(Integer clusterId);
    
    /**
     * Get all clusters
     * @return List of all clusters
     */
    List<Cluster> getAllClusters();
    
    /**
     * Get users in a cluster
     * @param clusterId Cluster ID
     * @return List of user profiles in the cluster
     */
    List<UserProfile> getUsersInCluster(Integer clusterId);
    
    /**
     * Create or update user profile from external data
     * @param userProfileDTO User profile data
     * @return Created or updated user profile
     */
    UserProfile createOrUpdateUserProfile(UserProfileDTO userProfileDTO);
}


