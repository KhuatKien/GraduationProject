package com.phenikaa.tourService.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folderName) throws IOException;

    void deleteImage(String publicId) throws IOException;

    void deleteFolder(String folderName) throws IOException;

    String getImageUrl(String imageName);

    String extractPublicIdFromUrl(String imageUrl);
}
