package com.phenikaa.userService.controller;

import com.phenikaa.userService.dto.request.UpdateUserStatusRequest;
import com.phenikaa.userService.dto.response.AdminUserResponse;
import com.phenikaa.userService.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserResponse> users = userService.getAllUsers(pageable, search, status);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails(@PathVariable Integer userId) {
        Optional<AdminUserResponse> user = userService.getUserDetailsForAdmin(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status) {
        try {
            List<AdminUserResponse> users = userService.getUsersByStatus(status);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Integer userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        try {
            Optional<AdminUserResponse> updatedUser = userService.updateUserStatus(userId, request);
            if (updatedUser.isPresent()) {
                return ResponseEntity.ok(updatedUser.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating user status");
        }
    }

    @PutMapping("/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> banUser(@PathVariable Integer userId) {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(com.phenikaa.userService.entity.AccountStatus.LOCKED);

        try {
            Optional<AdminUserResponse> updatedUser = userService.updateUserStatus(userId, request);
            if (updatedUser.isPresent()) {
                return ResponseEntity.ok(updatedUser.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while banning user");
        }
    }

    @PutMapping("/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unbanUser(@PathVariable Integer userId) {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(com.phenikaa.userService.entity.AccountStatus.ACTIVE);

        try {
            Optional<AdminUserResponse> updatedUser = userService.updateUserStatus(userId, request);
            if (updatedUser.isPresent()) {
                return ResponseEntity.ok(updatedUser.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while unbanning user");
        }
    }
}
