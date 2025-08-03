package com.phenikaa.authService.client;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<UserInfoResponse> verifyUser(LoginRequest request) {
        String userServiceUrl = "http://localhost:8083";
        return webClientBuilder.baseUrl(userServiceUrl)
                .build()
                .post()
                .uri("/internal/users/verify")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserInfoResponse.class);
    }
}