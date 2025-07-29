package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.request.AddTourRequest;
import com.phenikaa.tourService.dto.request.UpdateTourRequest;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;

import java.util.List;

public interface TourService {
    List<ViewTourResponse> getAllTours();
    List<ViewTourResponse> searchToursByKeywordAndFilter(String keyword, String filterBy);
    Tour addTour(Integer userId, AddTourRequest tour);
    Tour updateTour(UpdateTourRequest tour);
    ViewTourResponse viewTour(Integer tourId);

}
