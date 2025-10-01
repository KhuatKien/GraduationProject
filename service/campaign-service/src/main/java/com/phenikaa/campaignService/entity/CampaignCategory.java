package com.phenikaa.campaignService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}

