package com.phenikaa.userService.controller;

import com.phenikaa.userService.dto.request.UpdateProfileRequest;
import com.phenikaa.userService.dto.response.ProfileResponse;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Integer userId) {
        Optional<ProfileResponse> userProfile = userService.getProfile(userId);
        if (userProfile.isPresent()) {
            return ResponseEntity.ok(userProfile.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            // Lấy thông tin user hiện tại
            Optional<ProfileResponse> currentUser = userService.getProfile(userId);
            if (currentUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Validate avatar file if provided
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body("File must be an image");
                }
            }

            // Tạo UpdateProfileRequest với thông tin hiện tại
            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            ProfileResponse current = currentUser.get();

            // Chỉ cập nhật những field có giá trị mới
            updateRequest.setEmail(email != null ? email : current.getEmail());
            updateRequest.setPhoneNumber(phoneNumber != null ? phoneNumber : current.getPhoneNumber());
            updateRequest.setFullName(fullName != null ? fullName : current.getFullName());
            updateRequest.setAddress(address != null ? address : current.getAddress());

            // Xử lý dateOfBirth
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    updateRequest.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd");
                }
            } else {
                updateRequest.setDateOfBirth(current.getDateOfBirth());
            }

            // Update profile với hoặc không có avatar
            Optional<ProfileResponse> updatedUser = userService.updateProfileWithAvatar(userId, updateRequest,
                    avatarFile);
            if (updatedUser.isPresent()) {
                return ResponseEntity.ok(updatedUser.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload avatar: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating profile");
        }
    }
}
