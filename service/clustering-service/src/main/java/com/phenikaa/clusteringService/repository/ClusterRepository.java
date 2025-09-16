package com.phenikaa.clusteringService.repository;

import com.phenikaa.clusteringService.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Integer> {
    
    Optional<Cluster> findByClusterName(String clusterName);
    
    List<Cluster> findByIsActiveTrue();
    
    @Query("SELECT c FROM Cluster c WHERE c.isActive = true ORDER BY c.size DESC")
    List<Cluster> findActiveClustersOrderBySize();
}


