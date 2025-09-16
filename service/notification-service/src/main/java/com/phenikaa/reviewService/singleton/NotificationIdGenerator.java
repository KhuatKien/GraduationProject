package com.phenikaa.reviewService.singleton;

// Singleton đơn giản để tạo ID duy nhất cho notification
public class NotificationIdGenerator {

    // Eager initialization - đơn giản nhất
    private static final NotificationIdGenerator INSTANCE = new NotificationIdGenerator();

    private long counter = 0;

    // Private constructor
    private NotificationIdGenerator() {}


    // Lấy instance duy nhất

    public static NotificationIdGenerator getInstance() {
        return INSTANCE;
    }

    //Tạo ID duy nhất

    public synchronized String generateId() {
        counter++;
        return "NOTIF_" + System.currentTimeMillis() + "_" + counter;
    }
}
