package com.phenikaa.authService.service;

import com.phenikaa.authService.client.UserServiceClient;
import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.authService.dto.response.AuthResponse;
import com.phenikaa.dto.request.RefreshTokenRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
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

    // public Mono<AuthResponse> login(LoginRequest request) {
    // return userServiceClient.verifyUser(request)
    // .switchIfEmpty(Mono.error(new RuntimeException("User not found or password
    // wrong!")))
    // .flatMap(user -> {
    // var authorities = user.roles().stream()
    // .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
    // .collect(Collectors.toList());
    //
    // String accessToken = jwtUtil.generateAccessToken(
    // user.userName(), user.userId(), authorities
    // );
    //
    //
    // return Mono.just(new AuthResponse(accessToken));
    // });
    // }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userServiceClient.verifyUser(request)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found or password wrong!")))
                .flatMap(user -> {
                    var authorities = user.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    String accessToken = jwtUtil.generateAccessToken(user.userName(), user.userId(), authorities);
                    String refreshToken = jwtUtil.generateRefreshToken(user.userName(), user.userId());

                    SaveRefreshTokenRequest tokenRequest = new SaveRefreshTokenRequest();
                    tokenRequest.setRefreshToken(refreshToken);
                    tokenRequest.setUserId(user.userId());
                    tokenRequest.setExpiryDate(jwtUtil.getExpirationDateFromToken(refreshToken).toInstant());

                    return userServiceClient.saveRefreshToken(tokenRequest)
                            .thenReturn(new AuthResponse(accessToken, refreshToken));
                });
    }

    public Mono<AuthResponse> refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            return Mono.error(new RuntimeException("Refresh token is invalid or expired!"));
        }

        return userServiceClient.getUserByRefreshToken(refreshToken)
                .switchIfEmpty(Mono.error(new RuntimeException("Refresh token not found in DB!")))
                .flatMap(user -> {
                    var authorities = user.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    String accessToken = jwtUtil.generateAccessToken(user.userName(), user.userId(), authorities);

                    return Mono.just(new AuthResponse(accessToken, refreshToken));
                });
    }
}