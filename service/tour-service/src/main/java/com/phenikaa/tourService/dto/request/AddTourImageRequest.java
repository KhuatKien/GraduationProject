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
public class AddTourImageRequest {
    private MultipartFile imageFile;
    private String imageUrl; // URL từ Cloudinary sau khi upload
    private String caption;
    private Boolean isPrimary;
    private Integer sortOrder;
}
