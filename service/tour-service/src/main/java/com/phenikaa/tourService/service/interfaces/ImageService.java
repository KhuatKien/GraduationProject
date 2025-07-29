package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.request.UpdateTourImageRequest;
import com.phenikaa.tourService.entity.TourImage;

public interface ImageService {
    TourImage updateImage(UpdateTourImageRequest dto);
}
