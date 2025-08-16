package com.phenikaa.tourService.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folderName);
    void deleteImage(String imageName);
    String getImageUrl(String imageName);
}
