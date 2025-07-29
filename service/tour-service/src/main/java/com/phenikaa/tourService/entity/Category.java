package com.phenikaa.tourService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, updatable = false)
    private Integer categoryId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name; // DOMESTIC, INTERNATIONAL, FAMILY, COUPLE, ADVENTURE

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Tour> tours;
}
