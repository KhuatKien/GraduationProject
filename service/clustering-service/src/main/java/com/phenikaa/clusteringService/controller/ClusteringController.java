package com.phenikaa.clusteringService.controller;

import com.phenikaa.clusteringService.dto.UserProfileDTO;
import com.phenikaa.clusteringService.entity.Cluster;
import com.phenikaa.clusteringService.entity.UserProfile;
import com.phenikaa.clusteringService.service.ClusteringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clustering")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClusteringController {
    
    private final ClusteringService clusteringService;
    
    /**
     * Trigger clustering process
     */
    @PostMapping("/perform")
    public ResponseEntity<Map<String, Object>> performClustering() {
        try {
            Map<Integer, List<UserProfile>> clusters = clusteringService.performClustering();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Clustering completed successfully",
                "clustersCount", clusters.size(),
                "totalUsers", clusters.values().stream().mapToInt(List::size).sum()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Clustering failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user recommendations
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<Map<String, Object>> getRecommendations(@PathVariable Integer userId) {
        try {
            List<Integer> recommendations = clusteringService.getRecommendations(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "recommendations", recommendations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get recommendations: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all clusters
     */
    @GetMapping("/clusters")
    public ResponseEntity<Map<String, Object>> getAllClusters() {
        try {
            List<Cluster> clusters = clusteringService.getAllClusters();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "clusters", clusters
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get clusters: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get cluster details
     */
    @GetMapping("/clusters/{clusterId}")
    public ResponseEntity<Map<String, Object>> getClusterDetails(@PathVariable Integer clusterId) {
        try {
            Cluster cluster = clusteringService.getClusterInfo(clusterId);
            List<UserProfile> users = clusteringService.getUsersInCluster(clusterId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cluster", cluster,
                "users", users
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get cluster details: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Create or update user profile
     */
    @PostMapping("/profiles")
    public ResponseEntity<Map<String, Object>> createOrUpdateProfile(@RequestBody UserProfileDTO userProfileDTO) {
        try {
            UserProfile profile = clusteringService.createOrUpdateUserProfile(userProfileDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User profile updated successfully",
                "profile", profile
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update profile: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update user cluster assignment
     */
    @PutMapping("/profiles/{userId}/cluster")
    public ResponseEntity<Map<String, Object>> updateUserCluster(@PathVariable Integer userId) {
        try {
            UserProfile profile = clusteringService.updateUserCluster(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User cluster updated successfully",
                "profile", profile
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update user cluster: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get clustering statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getClusteringStats() {
        try {
            List<Cluster> clusters = clusteringService.getAllClusters();
            
            Map<String, Object> stats = Map.of(
                "totalClusters", clusters.size(),
                "totalUsers", clusters.stream().mapToInt(Cluster::getSize).sum(),
                "averageClusterSize", clusters.isEmpty() ? 0 : 
                    clusters.stream().mapToInt(Cluster::getSize).sum() / clusters.size(),
                "clusters", clusters.stream().map(cluster -> Map.of(
                    "id", cluster.getClusterId(),
                    "name", cluster.getClusterName(),
                    "size", cluster.getSize(),
                    "description", cluster.getDescription()
                )).toList()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get clustering stats: " + e.getMessage()
            ));
        }
    }
}


