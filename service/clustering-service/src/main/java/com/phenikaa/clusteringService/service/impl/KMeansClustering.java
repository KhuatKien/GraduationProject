package com.phenikaa.clusteringService.service.impl;

import com.phenikaa.clusteringService.entity.UserProfile;
import com.phenikaa.clusteringService.service.ClusteringAlgorithm;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KMeansClustering implements ClusteringAlgorithm {
    
    private static final int DEFAULT_K = 5;
    private static final int MAX_ITERATIONS = 100;
    
    @Override
    public Map<Integer, List<UserProfile>> cluster(List<UserProfile> profiles) {
        if (profiles.isEmpty()) {
            return new HashMap<>();
        }
        
        int k = Math.min(DEFAULT_K, profiles.size());
        List<double[]> centroids = initializeCentroids(profiles, k);
        Map<Integer, List<UserProfile>> clusters = new HashMap<>();
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Assign profiles to nearest centroid
            clusters = assignProfilesToClusters(profiles, centroids);
            
            // Update centroids
            List<double[]> newCentroids = updateCentroids(clusters);
            
            // Check for convergence
            if (hasConverged(centroids, newCentroids)) {
                break;
            }
            
            centroids = newCentroids;
        }
        
        return clusters;
    }
    
    @Override
    public Integer assignToCluster(UserProfile profile, Map<Integer, List<UserProfile>> existingClusters) {
        if (existingClusters.isEmpty()) {
            return 0;
        }
        
        double[] profileVector = extractFeatureVector(profile);
        double minDistance = Double.MAX_VALUE;
        Integer bestCluster = 0;
        
        for (Map.Entry<Integer, List<UserProfile>> entry : existingClusters.entrySet()) {
            double[] centroid = calculateCentroid(entry.getValue());
            double distance = calculateDistance(profileVector, centroid);
            
            if (distance < minDistance) {
                minDistance = distance;
                bestCluster = entry.getKey();
            }
        }
        
        return bestCluster;
    }
    
    @Override
    public String getAlgorithmName() {
        return "K-Means";
    }
    
    private List<double[]> initializeCentroids(List<UserProfile> profiles, int k) {
        List<double[]> centroids = new ArrayList<>();
        Random random = new Random();
        
        // Randomly select k profiles as initial centroids
        List<UserProfile> shuffled = new ArrayList<>(profiles);
        Collections.shuffle(shuffled, random);
        
        for (int i = 0; i < k; i++) {
            centroids.add(extractFeatureVector(shuffled.get(i)));
        }
        
        return centroids;
    }
    
    private Map<Integer, List<UserProfile>> assignProfilesToClusters(List<UserProfile> profiles, List<double[]> centroids) {
        Map<Integer, List<UserProfile>> clusters = new HashMap<>();
        
        for (int i = 0; i < centroids.size(); i++) {
            clusters.put(i, new ArrayList<>());
        }
        
        for (UserProfile profile : profiles) {
            double[] profileVector = extractFeatureVector(profile);
            double minDistance = Double.MAX_VALUE;
            int bestCluster = 0;
            
            for (int i = 0; i < centroids.size(); i++) {
                double distance = calculateDistance(profileVector, centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    bestCluster = i;
                }
            }
            
            clusters.get(bestCluster).add(profile);
        }
        
        return clusters;
    }
    
    private List<double[]> updateCentroids(Map<Integer, List<UserProfile>> clusters) {
        List<double[]> newCentroids = new ArrayList<>();
        
        for (Map.Entry<Integer, List<UserProfile>> entry : clusters.entrySet()) {
            List<UserProfile> clusterProfiles = entry.getValue();
            if (!clusterProfiles.isEmpty()) {
                newCentroids.add(calculateCentroid(clusterProfiles));
            }
        }
        
        return newCentroids;
    }
    
    private double[] calculateCentroid(List<UserProfile> profiles) {
        if (profiles.isEmpty()) {
            return new double[0];
        }
        
        double[] centroid = new double[getFeatureVectorSize()];
        
        for (UserProfile profile : profiles) {
            double[] vector = extractFeatureVector(profile);
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] += vector[i];
            }
        }
        
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= profiles.size();
        }
        
        return centroid;
    }
    
    private boolean hasConverged(List<double[]> oldCentroids, List<double[]> newCentroids) {
        if (oldCentroids.size() != newCentroids.size()) {
            return false;
        }
        
        double threshold = 0.001;
        for (int i = 0; i < oldCentroids.size(); i++) {
            if (calculateDistance(oldCentroids.get(i), newCentroids.get(i)) > threshold) {
                return false;
            }
        }
        
        return true;
    }
    
    private double[] extractFeatureVector(UserProfile profile) {
        double[] vector = new double[getFeatureVectorSize()];
        int index = 0;
        
        // Age group (one-hot encoding)
        vector[index++] = "YOUNG".equals(profile.getAgeGroup()) ? 1.0 : 0.0;
        vector[index++] = "ADULT".equals(profile.getAgeGroup()) ? 1.0 : 0.0;
        vector[index++] = "MIDDLE".equals(profile.getAgeGroup()) ? 1.0 : 0.0;
        vector[index++] = "SENIOR".equals(profile.getAgeGroup()) ? 1.0 : 0.0;
        
        // Region (one-hot encoding)
        vector[index++] = "NORTH".equals(profile.getRegion()) ? 1.0 : 0.0;
        vector[index++] = "CENTRAL".equals(profile.getRegion()) ? 1.0 : 0.0;
        vector[index++] = "SOUTH".equals(profile.getRegion()) ? 1.0 : 0.0;
        
        // Tour type (one-hot encoding)
        vector[index++] = "ADVENTURE".equals(profile.getPreferredTourType()) ? 1.0 : 0.0;
        vector[index++] = "CULTURAL".equals(profile.getPreferredTourType()) ? 1.0 : 0.0;
        vector[index++] = "RELAXATION".equals(profile.getPreferredTourType()) ? 1.0 : 0.0;
        vector[index++] = "FAMILY".equals(profile.getPreferredTourType()) ? 1.0 : 0.0;
        
        // Season (one-hot encoding)
        vector[index++] = "SPRING".equals(profile.getPreferredSeason()) ? 1.0 : 0.0;
        vector[index++] = "SUMMER".equals(profile.getPreferredSeason()) ? 1.0 : 0.0;
        vector[index++] = "AUTUMN".equals(profile.getPreferredSeason()) ? 1.0 : 0.0;
        vector[index++] = "WINTER".equals(profile.getPreferredSeason()) ? 1.0 : 0.0;
        
        // Group size (one-hot encoding)
        vector[index++] = "SOLO".equals(profile.getGroupSizePreference()) ? 1.0 : 0.0;
        vector[index++] = "COUPLE".equals(profile.getGroupSizePreference()) ? 1.0 : 0.0;
        vector[index++] = "SMALL_GROUP".equals(profile.getGroupSizePreference()) ? 1.0 : 0.0;
        vector[index++] = "LARGE_GROUP".equals(profile.getGroupSizePreference()) ? 1.0 : 0.0;
        
        // Booking lead time (one-hot encoding)
        vector[index++] = "LAST_MINUTE".equals(profile.getBookingLeadTime()) ? 1.0 : 0.0;
        vector[index++] = "SHORT".equals(profile.getBookingLeadTime()) ? 1.0 : 0.0;
        vector[index++] = "MEDIUM".equals(profile.getBookingLeadTime()) ? 1.0 : 0.0;
        vector[index++] = "LONG".equals(profile.getBookingLeadTime()) ? 1.0 : 0.0;
        
        // Numeric features (normalized)
        vector[index++] = normalizeSpending(profile.getAverageSpending());
        vector[index++] = normalizePromotionResponsiveness(profile.getPromotionResponsiveness());
        
        return vector;
    }
    
    private int getFeatureVectorSize() {
        return 4 + 3 + 4 + 4 + 4 + 4 + 2; // age + region + tour_type + season + group_size + lead_time + numeric
    }
    
    private double normalizeSpending(Double spending) {
        if (spending == null) return 0.0;
        // Normalize to 0-1 range (assuming max spending is 10,000,000 VND)
        return Math.min(spending / 10000000.0, 1.0);
    }
    
    private double normalizePromotionResponsiveness(Double responsiveness) {
        if (responsiveness == null) return 0.0;
        return Math.max(0.0, Math.min(1.0, responsiveness));
    }
    
    private double calculateDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }
        
        return Math.sqrt(sum);
    }
}


