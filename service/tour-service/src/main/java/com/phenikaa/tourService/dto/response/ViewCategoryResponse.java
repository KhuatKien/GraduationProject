package com.phenikaa.tourService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewCategoryResponse {
    private Integer categoryId;
    private String name;
    private String description;
}
