package com.phenikaa.userService.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phenikaa.userService.service.interfaces.CloudinaryService;
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
    public String extractPublicIdFromUrl(String imageUrl) {
        // Extract public ID from Cloudinary URL including folder path
        // Example:
        // https://res.cloudinary.com/<cloud>/image/upload/v1700000000/avatars/username/avatar.jpg
        // public_id => avatars/username/avatar
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        final String uploadMarker = "/upload/";
        int markerIndex = imageUrl.indexOf(uploadMarker);
        if (markerIndex == -1) {
            // Fallback: use last segment without extension
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        }

        String path = imageUrl.substring(markerIndex + uploadMarker.length());
        int queryIdx = path.indexOf('?');
        if (queryIdx != -1) {
            path = path.substring(0, queryIdx);
        }
        // Strip version segment like v1700000000/
        if (path.startsWith("v")) {
            int slashIdx = path.indexOf('/');
            if (slashIdx > 1) {
                boolean digits = path.substring(1, slashIdx).chars().allMatch(Character::isDigit);
                if (digits) {
                    path = path.substring(slashIdx + 1);
                }
            }
        }
        // Remove extension
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            path = path.substring(0, lastDot);
        }
        return path;
    }
}
