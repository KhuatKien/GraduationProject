package com.phenikaa.tourService.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourImageRequest {
    private Integer imageId;
    private String imageUrl;
    private String caption;
    private Boolean isPrimary;
    private Integer sortOrder;
}
