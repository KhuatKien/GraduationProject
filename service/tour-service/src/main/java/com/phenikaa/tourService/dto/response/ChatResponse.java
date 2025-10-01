package com.phenikaa.tourService.dto.response;

import com.phenikaa.tourService.projection.TourSummaryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String sessionId;
    private String responseType; // "general", "topic_suggestion", "lecturer_suggestion", "capacity_check"
    private List<TourSummaryProjection> tours; // Tour data for frontend cards
}
