package com.phenikaa.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetInfoTour {
    private Integer availableSlots;
    private Double adultPrice;
    private Double childPrice;
    private String categoryName;
}

