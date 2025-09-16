package com.phenikaa.clusteringService.repository;

import com.phenikaa.clusteringService.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUserId(Integer userId);
    
    List<UserProfile> findByClusterId(Integer clusterId);
    
    @Query("SELECT up FROM UserProfile up WHERE up.clusterId IS NULL")
    List<UserProfile> findUnclusteredProfiles();
    
    @Query("SELECT up FROM UserProfile up WHERE up.clusterId = :clusterId ORDER BY up.clusterConfidence DESC")
    List<UserProfile> findByClusterIdOrderByConfidence(@Param("clusterId") Integer clusterId);
    
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.clusterId = :clusterId")
    Long countByClusterId(@Param("clusterId") Integer clusterId);
    
    @Query("SELECT DISTINCT up.clusterId FROM UserProfile up WHERE up.clusterId IS NOT NULL")
    List<Integer> findDistinctClusterIds();
}


