package com.phenikaa.userService.controller;

import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/saveUser")
    public ResponseEntity<User> saveUser(@RequestBody RegisterRequest registerRequest) {
        User savedUser = userService.save(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
