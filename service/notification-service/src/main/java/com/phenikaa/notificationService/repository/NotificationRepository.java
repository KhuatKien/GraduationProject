package com.phenikaa.notificationService.repository;

import com.phenikaa.notificationService.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, Integer> {
    Flux<Notification> findByReceiverId(Integer receiverId);
}
