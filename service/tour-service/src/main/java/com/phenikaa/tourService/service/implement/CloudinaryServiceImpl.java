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

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "image",
                    "quality", "auto:good",
                    "fetch_format", "auto");

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFolder(String folderName) throws IOException {
        try {
            // Get all resources in the folder
            Map<String, Object> searchResult = cloudinary.search()
                    .expression("folder:" + folderName)
                    .maxResults(500)
                    .execute();

            if (searchResult.get("resources") != null) {
                // Fix casting issue: resources là ArrayList, không phải Object[]
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> resources = (java.util.List<Map<String, Object>>) searchResult.get("resources");

                // Delete each image
                for (Map<String, Object> resource : resources) {
                    String publicId = (String) resource.get("public_id");
                    if (publicId != null) {
                        deleteImage(publicId);
                        System.out.println("Deleted resource from folder: " + publicId);
                    }
                }
            }

            // Delete the folder itself (chỉ khi folder rỗng)
            cloudinary.api().deleteFolder(folderName, ObjectUtils.emptyMap());
            System.out.println("Deleted folder: " + folderName);

        } catch (Exception e) {
            throw new IOException("Failed to delete folder from Cloudinary: " + e.getMessage(), e);
        }
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
