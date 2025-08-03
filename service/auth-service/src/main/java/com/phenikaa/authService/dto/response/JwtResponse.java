package com.phenikaa.authService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    String accessToken;
    String refreshToken;
    String username;
    List<String> roles;
}
