package com.phenikaa.userService.service.interfaces;

import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;

public interface RefreshTokenService {
    void saveRefreshToken(SaveRefreshTokenRequest request);
    void deleteByRefreshToken(String refreshToken);
    UserInfoResponse getUserByRefreshToken(String token);
}
