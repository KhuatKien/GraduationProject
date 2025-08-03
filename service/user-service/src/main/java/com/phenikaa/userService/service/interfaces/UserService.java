package com.phenikaa.userService.service.interfaces;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.CreateUserRequest;
import com.phenikaa.userService.entity.User;

import java.util.Optional;

public interface UserService {
    User save(CreateUserRequest user);
    Optional<UserInfoResponse> verifyUser(LoginRequest request);
}
