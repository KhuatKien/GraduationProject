package com.phenikaa.tourService.service.implement;

import com.phenikaa.tourService.dto.request.UpdateTourImageRequest;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.mapper.UpdateTourImageMapper;
import com.phenikaa.tourService.repository.ImageRepository;
import com.phenikaa.tourService.service.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UpdateTourImageMapper updateTourImageMapper;

    @Override
    public TourImage updateImage(UpdateTourImageRequest dto) {

        TourImage entity = imageRepository.findById(dto.getImageId())
                .orElseThrow(() -> new RuntimeException("Image not found"));

        updateTourImageMapper.updateEntity(dto, entity);
        entity.setImageId(dto.getImageId());
        return imageRepository.save(entity);
    }

}