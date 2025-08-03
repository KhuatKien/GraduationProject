package com.phenikaa.userService.controller;

import com.phenikaa.userService.dto.request.CreateUserRequest;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/saveUser")
    public ResponseEntity<User> saveUser(@RequestBody CreateUserRequest createUserRequest) {
        User savedUser = userService.save(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


}
