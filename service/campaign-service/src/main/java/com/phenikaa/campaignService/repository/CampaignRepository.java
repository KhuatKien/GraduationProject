package com.phenikaa.campaignService.repository;

import com.phenikaa.campaignService.entity.Campaign;
import com.phenikaa.campaignService.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

    // Tìm campaign theo status
    List<Campaign> findByStatus(CampaignStatus status);

    // Tìm campaign đang active
    List<Campaign> findByIsActiveTrue();

    // Tìm campaign theo category
    @Query("SELECT DISTINCT c FROM Campaign c JOIN c.targetCategories cc WHERE cc.categoryName = :categoryName AND c.isActive = true")
    List<Campaign> findByTargetCategory(@Param("categoryName") String categoryName);

    // Tìm campaign đang chạy trong khoảng thời gian
    @Query("SELECT c FROM Campaign c WHERE c.startDate <= :currentTime AND c.endDate >= :currentTime AND c.isActive = true")
    List<Campaign> findActiveCampaignsAtTime(@Param("currentTime") Instant currentTime);

    // Tìm campaign theo tên (tìm kiếm không phân biệt hoa thường)
    List<Campaign> findByNameContainingIgnoreCase(String name);

    // Tìm campaign theo khoảng thời gian
    @Query("SELECT c FROM Campaign c WHERE c.startDate >= :startDate AND c.endDate <= :endDate")
    List<Campaign> findByDateRange(@Param("startDate") Instant startDate,
                                   @Param("endDate") Instant endDate);

    // Tìm campaign sắp hết hạn
    @Query("SELECT c FROM Campaign c WHERE c.endDate <= :thresholdTime AND c.status = 'ACTIVE'")
    List<Campaign> findExpiringCampaigns(@Param("thresholdTime") Instant thresholdTime);

    // Đếm số campaign theo status
    long countByStatus(CampaignStatus status);

    // Tìm campaign theo createdBy
    List<Campaign> findByCreatedBy(String createdBy);
}
