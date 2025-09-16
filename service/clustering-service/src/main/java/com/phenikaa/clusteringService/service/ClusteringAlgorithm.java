package com.phenikaa.clusteringService.service;

import com.phenikaa.clusteringService.entity.UserProfile;
import java.util.List;
import java.util.Map;

public interface ClusteringAlgorithm {
    
    /**
     * Perform clustering on user profiles
     * @param profiles List of user profiles to cluster
     * @return Map of cluster ID to list of user profiles
     */
    Map<Integer, List<UserProfile>> cluster(List<UserProfile> profiles);
    
    /**
     * Assign a single user profile to a cluster
     * @param profile User profile to assign
     * @param existingClusters Map of existing clusters
     * @return Cluster ID assigned to the profile
     */
    Integer assignToCluster(UserProfile profile, Map<Integer, List<UserProfile>> existingClusters);
    
    /**
     * Get algorithm name
     * @return Algorithm name
     */
    String getAlgorithmName();
}


