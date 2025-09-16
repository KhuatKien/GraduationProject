package com.phenikaa.clusteringService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clusters")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_id")
    private Integer clusterId;

    @Column(name = "cluster_name", nullable = false)
    private String clusterName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics; // JSON string of cluster characteristics

    @Column(name = "size")
    private Integer size; // Number of users in this cluster

    @Column(name = "centroid", columnDefinition = "TEXT")
    private String centroid; // JSON string of cluster centroid

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


