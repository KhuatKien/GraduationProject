package com.phenikaa.reviewService.repository;

import com.phenikaa.reviewService.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, Integer> {
    Flux<Notification> findByReceiverId(Integer receiverId);
}