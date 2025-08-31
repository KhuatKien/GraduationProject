package com.phenikaa.tourService.service.interfaces;

import com.phenikaa.tourService.dto.request.AddTourRequest;
import com.phenikaa.tourService.dto.request.SearchTourCriteria;
import com.phenikaa.tourService.dto.request.UpdateTourRequest;
import com.phenikaa.tourService.dto.response.ViewTourResponse;
import com.phenikaa.tourService.entity.Tour;
import com.phenikaa.tourService.projection.TourSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface TourService {
    List<ViewTourResponse> searchToursByKeywordAndFilter(String keyword, String filterBy);

    Page<ViewTourResponse> getAllTours(Pageable pageable);

    Page<ViewTourResponse> searchToursByKeywordAndFilterWithPagination(String keyword, String filterBy,
            Pageable pageable);

    Page<ViewTourResponse> searchToursByQbe(SearchTourCriteria criteria, Pageable pageable);

    // QBE methods + pagination
    Page<ViewTourResponse> searchToursByExample(SearchTourCriteria criteria, Pageable pageable);

    // Specification methods + pagination
    Page<ViewTourResponse> searchToursBySpecification(SearchTourCriteria criteria, Pageable pageable);

    Tour addTour(Integer userId, AddTourRequest tour) throws IOException;

    Tour updateTourWithFiles(Integer tourId, Integer userId, UpdateTourRequest request) throws IOException;

    ViewTourResponse viewTour(Integer tourId);

    void deleteTour(Integer tourId);

    Page<TourSummaryProjection> getAllActiveToursSummary(Pageable pageable);

    Page<TourSummaryProjection> getAllActiveToursTimeBased(int page);
}
