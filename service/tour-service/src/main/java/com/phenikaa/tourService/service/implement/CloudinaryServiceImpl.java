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
    public String uploadImage(MultipartFile file, String folderName) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folderName // chỉ định folder
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại", e);
        }
    }

    @Override
    public void deleteImage(String imageName) {

    }

    @Override
    public String getImageUrl(String imageName) {
        return "";
    }
}
