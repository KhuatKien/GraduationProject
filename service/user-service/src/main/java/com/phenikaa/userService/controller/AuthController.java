package com.phenikaa.userService.controller;

import com.phenikaa.userService.config.CustomUserDetails;
import com.phenikaa.userService.dto.response.AuthResponse;
import com.phenikaa.userService.security.JwtTokenProvider;
import com.phenikaa.userService.dao.interfaces.UserDao;
import com.phenikaa.userService.dto.response.JwtResponse;
import com.phenikaa.userService.dto.request.LoginRequest;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/test")
    public ResponseEntity<?> register() {
        String password = "123456";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);
        return ResponseEntity.ok(encodedPassword);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Ép kiểu đúng để lấy userId
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String actualRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");

        String accessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getUsername(),
                userDetails.getUserId(),
                userDetails.getAuthorities()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

        // Log để kiểm tra
        System.out.println("userId: " + userDetails.getUserId());
        System.out.println("accessToken: " + accessToken);

        AuthResponse authResponse = new AuthResponse(userDetails.getUserId(),accessToken, refreshToken, actualRole);

        return ResponseEntity.ok(authResponse);
    }
}
