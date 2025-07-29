package com.phenikaa.tourService.controller;

import com.phenikaa.tourService.dto.request.UpdateTourImageRequest;
import com.phenikaa.tourService.entity.TourImage;
import com.phenikaa.tourService.service.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/updateImage")
    public TourImage updateImage(@RequestBody UpdateTourImageRequest dto) {
        return imageService.updateImage(dto);
    }
}
