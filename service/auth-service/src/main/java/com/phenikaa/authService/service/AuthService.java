package com.phenikaa.authService.service;

import com.phenikaa.authService.client.UserServiceClient;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authService.dto.response.AuthResponse;
import com.phenikaa.utils.JwtUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    public AuthService(UserServiceClient userServiceClient, JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userServiceClient.verifyUser(request)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found or password wrong!")))
                .flatMap(user -> {
                    var authorities = user.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    String accessToken = jwtUtil.generateAccessToken(
                            user.userName(), user.userId(), authorities
                    );

                    return Mono.just(new AuthResponse(accessToken));
                });
    }
}