package com.phenikaa.tourService.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phenikaa.tourService.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folderName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is null or empty");
        }

        System.out.println("🔄 Starting Cloudinary upload for file: " + file.getOriginalFilename());
        System.out.println("📁 Target folder: " + folderName);
        System.out.println("📏 File size: " + file.getSize() + " bytes");

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "image",
                    "quality", "auto:good",
                    "fetch_format", "auto");

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            System.out.println("✅ Upload successful!");
            System.out.println("🔗 URL: " + url);
            System.out.println("🆔 Public ID: " + publicId);

            return url;
        } catch (Exception e) {
            System.err.println("❌ Cloudinary upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    @Override
    public String getImageUrl(String imageName) {
        return cloudinary.url().generate(imageName);
    }

    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        // Extract public ID from Cloudinary URL
        String[] parts = imageUrl.split("/");
        String fileName = parts[parts.length - 1];
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
