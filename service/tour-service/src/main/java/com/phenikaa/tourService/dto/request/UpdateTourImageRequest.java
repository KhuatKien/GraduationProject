package com.phenikaa.tourService.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourImageRequest {
    private Integer imageId; // Cho existing images
    private MultipartFile imageFile; // Cho new images upload
    private String imageUrl; // URL từ Cloudinary (existing) hoặc sau khi upload (new)
    private String caption;
    private Boolean isPrimary;
    private Integer sortOrder;
}
