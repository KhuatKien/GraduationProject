package com.phenikaa.userService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Integer userId;
    private String accessToken;
    private String refreshToken;
    private String role;
}