package com.phenikaa.userService.service.implement;

import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.entity.RefreshToken;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.mapper.UserMapper;
import com.phenikaa.userService.repository.RefreshTokenRepository;
import com.phenikaa.userService.repository.UserRepository;
import com.phenikaa.userService.service.interfaces.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public void saveRefreshToken(SaveRefreshTokenRequest request) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser_UserId(request.getUserId());

        if (existingToken.isPresent() && existingToken.get().getExpiryDate().isAfter(Instant.now())) {
            return;
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = existingToken.orElse(new RefreshToken());

        refreshToken.setRefreshToken(request.getRefreshToken());
        refreshToken.setExpiryDate(request.getExpiryDate());
        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void deleteByRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    @Override
    public UserInfoResponse getUserByRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isAfter(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        return userMapper.toUserInfoResponse(user);
    }
}
