package com.phenikaa.userService.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.request.SaveRefreshTokenRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.service.interfaces.RefreshTokenService;
import com.phenikaa.userService.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
@Validated
public class InternalUserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User savedUser = userService.save(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while registering user");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<UserInfoResponse> verifyUser(@RequestBody LoginRequest request) {
        return userService.verifyUser(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(@PathVariable Integer userId) {
        return userService.getUserInfoById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/saveRefreshToken")
    public ResponseEntity<Void> saveRefreshToken(@RequestBody SaveRefreshTokenRequest request) {
        refreshTokenService.saveRefreshToken(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteRefreshToken")
    public ResponseEntity<Void> deleteRefreshToken(@RequestParam String token) {
        refreshTokenService.deleteByRefreshToken(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getUserByRefreshToken")
    public ResponseEntity<UserInfoResponse> getUserByRefreshToken(@RequestParam String token) {
        UserInfoResponse response = refreshTokenService.getUserByRefreshToken(token);
        return ResponseEntity.ok(response);
    }
}
