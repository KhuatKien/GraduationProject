package com.phenikaa.campaignService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignListResponse {

    private List<CampaignResponse> campaigns;
    private long totalCount;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static CampaignListResponse of(List<CampaignResponse> campaigns, long totalCount,
            int pageNumber, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        return CampaignListResponse.builder()
                .campaigns(campaigns)
                .totalCount(totalCount)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(pageNumber < totalPages - 1)
                .hasPrevious(pageNumber > 0)
                .build();
    }
}
