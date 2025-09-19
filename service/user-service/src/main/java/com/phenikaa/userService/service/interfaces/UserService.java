package com.phenikaa.userService.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.dto.request.UpdateProfileRequest;
import com.phenikaa.userService.dto.request.UpdateUserStatusRequest;
import com.phenikaa.userService.dto.response.AdminUserResponse;
import com.phenikaa.userService.dto.response.ProfileResponse;
import com.phenikaa.userService.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(RegisterRequest registerRequest);

    Optional<UserInfoResponse> verifyUser(LoginRequest request);

    Optional<UserInfoResponse> getUserInfoById(Integer userId);

    Optional<ProfileResponse> getProfile(Integer userId);

    Optional<ProfileResponse> updateProfile(Integer userId, UpdateProfileRequest updateProfileRequest);

    Optional<ProfileResponse> updateProfileWithAvatar(Integer userId, UpdateProfileRequest updateProfileRequest,
            MultipartFile avatarFile) throws IOException;

    // Admin methods
    List<AdminUserResponse> getAllUsers();

    Page<AdminUserResponse> getAllUsers(Pageable pageable, String search, String status);

    Optional<AdminUserResponse> getUserDetailsForAdmin(Integer userId);

    Optional<AdminUserResponse> updateUserStatus(Integer userId, UpdateUserStatusRequest request);

    List<AdminUserResponse> getUsersByStatus(String status);
}
