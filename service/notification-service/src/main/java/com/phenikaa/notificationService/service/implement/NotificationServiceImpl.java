package com.phenikaa.notificationService.service.implement;

import com.phenikaa.notificationService.broadcaster.NotificationBroadcaster;
import com.phenikaa.notificationService.entity.Notification;
import com.phenikaa.notificationService.entity.NotificationType;
import com.phenikaa.notificationService.repository.NotificationRepository;
import com.phenikaa.notificationService.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
//    private final NotificationPublisher notificationBroadcaster;
    private final ReactiveMongoTemplate mongoTemplate;
    private final NotificationBroadcaster notificationBroadcaster;

    @Override
    public Mono<Notification> createNotification(Integer senderId, Integer receiverId, String title, String message, NotificationType type, String actionUrl) {
        Notification notice = Notification.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .title(title)
                .message(message)
                .type(type)
                .actionUrl(actionUrl)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        return notificationRepository.save(notice)
                .doOnSuccess(saved -> notificationBroadcaster.publish(receiverId, saved));
    }

    @Override
    public Mono<Long> markAllAsReadAndPublish(Integer receiverId) {
        Query query = new Query(
                Criteria.where("receiverId").is(receiverId)
                        .and("read").is(false)
        );

        return mongoTemplate.find(query, Notification.class)
                .collectList()
                .flatMap(notifications -> {
                    if (notifications.isEmpty()) {
                        return Mono.just(0L);
                    }

                    Update update = new Update().set("read", true);
                    return mongoTemplate.updateMulti(query, update, Notification.class)
                            .map(result -> {
                                notifications.forEach(noti -> {
                                    noti.setRead(true); // update lại local
                                    notificationBroadcaster.publish(receiverId, noti);
                                });
                                return (long) result.getModifiedCount();
                            });
                });
    }


    @Override
    public Mono<Notification> toggleReadAndPublish(Integer receiverId, String notificationId) {
        Query query = new Query(Criteria.where("receiverId").is(receiverId)
                .and("_id").is(notificationId));

        return mongoTemplate.findOne(query, Notification.class)
                .flatMap(existing -> {
                    boolean newValue = !existing.isRead();
                    Update update = new Update().set("read", newValue);

                    return mongoTemplate.findAndModify(query, update, Notification.class)
                            .doOnSuccess(updatedNoti -> {
                                if (updatedNoti != null) {
                                    notificationBroadcaster.publish(receiverId, updatedNoti);
                                }
                            });
                });
    }

}
