package com.phenikaa.reviewService.entity;

public enum NotificationType {

    // Booking related notifications
    TOUR_BOOKED,
    BOOKING_CONFIRM,
    BOOKING_CANCELLED,

    // Tour related notifications
    TOUR_REMINDER,
    TOUR_STARTED,
    TOUR_COMPLETED,

    // Marketing notifications
    PROMOTION,

    // Payment related notifications
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // System notifications
    SYSTEM;
}
