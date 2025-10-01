package com.phenikaa.campaignService.enums;

public enum CampaignStatus {
    DRAFT("Draft", "Campaign đang được tạo"),
    ACTIVE("Active", "Campaign đang hoạt động"),
    PAUSED("Paused", "Campaign tạm dừng"),
    EXPIRED("Expired", "Campaign đã hết hạn"),
    CANCELLED("Cancelled", "Campaign đã hủy");

    private final String displayName;
    private final String description;

    CampaignStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Kiểm tra xem status có phải là active không
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Kiểm tra xem status có thể được kích hoạt không
     */
    public boolean canBeActivated() {
        return this == DRAFT || this == PAUSED;
    }

    /**
     * Kiểm tra xem status có thể được tạm dừng không
     */
    public boolean canBePaused() {
        return this == ACTIVE;
    }

    /**
     * Kiểm tra xem status có thể được hủy không
     */
    public boolean canBeCancelled() {
        return this == DRAFT || this == ACTIVE || this == PAUSED;
    }

    /**
     * Kiểm tra xem status có phải là final state không (không thể thay đổi)
     */
    public boolean isFinalState() {
        return this == EXPIRED || this == CANCELLED;
    }
}
