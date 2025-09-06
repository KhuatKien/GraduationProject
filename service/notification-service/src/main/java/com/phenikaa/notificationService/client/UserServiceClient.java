package com.phenikaa.notificationService.client;

import com.phenikaa.notificationService.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final String userServiceUrl = "http://localhost:8083";

    public Mono<UserInfoResponse> getUserById(Integer userId) {
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserInfoResponse.class);
    }
}
