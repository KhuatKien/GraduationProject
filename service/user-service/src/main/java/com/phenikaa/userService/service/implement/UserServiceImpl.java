package com.phenikaa.userService.service.implement;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.dto.request.UpdateProfileRequest;
import com.phenikaa.userService.dto.request.UpdateUserStatusRequest;
import com.phenikaa.userService.dto.response.AdminUserResponse;
import com.phenikaa.userService.dto.response.ProfileResponse;
import com.phenikaa.userService.entity.AccountStatus;
import com.phenikaa.userService.entity.Role;
import com.phenikaa.userService.entity.RoleName;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.mapper.UserMapper;
import com.phenikaa.userService.repository.RoleRepository;
import com.phenikaa.userService.repository.UserRepository;
import com.phenikaa.userService.service.interfaces.CloudinaryService;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public User save(RegisterRequest registerRequest) {
        Optional<User> userExist = userRepository.findByUserNameOrEmail(registerRequest.getUserName(),
                registerRequest.getEmail());
        if (userExist.isPresent()) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        User user = userMapper.toEntity(registerRequest);

        if (registerRequest.getPassword() != null && !registerRequest.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
            user.setPassword(encodedPassword);
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Gán role mặc định CUSTOMER từ DB
        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Default role CUSTOMER not found. Seed roles first."));

        user.getRoles().clear();
        user.getRoles().add(customerRole);

        // Có thể đảm bảo trạng thái mặc định nếu cần
        if (user.getStatus() == null) {
            user.setStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        return user;
    }

    public Optional<UserInfoResponse> verifyUser(LoginRequest request) {
        return userRepository.findByUserName(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> new UserInfoResponse(
                        user.getUserId(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName().name())
                                .collect(Collectors.toList())));
    }

    @Override
    public Optional<UserInfoResponse> getUserInfoById(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> new UserInfoResponse(
                        user.getUserId(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName().name())
                                .collect(Collectors.toList())));
    }

    @Override
    public Optional<ProfileResponse> getProfile(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> new ProfileResponse(
                        user.getUserId(),
                        user.getUserName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getFullName(),
                        user.getAvatar(),
                        user.getDateOfBirth(),
                        user.getAddress(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName().name())
                                .collect(Collectors.toList()),
                        user.getCreatedAt(),
                        user.getUpdatedAt()));
    }

    @Override
    public Optional<ProfileResponse> updateProfile(Integer userId, UpdateProfileRequest updateProfileRequest) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Kiểm tra email trùng lặp nếu có thay đổi email
                    if (updateProfileRequest.getEmail() != null &&
                            !updateProfileRequest.getEmail().equals(user.getEmail())) {
                        Optional<User> existingUser = userRepository.findByEmail(updateProfileRequest.getEmail());
                        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Email already exists");
                        }
                    }

                    // Kiểm tra số điện thoại trùng lặp nếu có thay đổi số điện thoại
                    if (updateProfileRequest.getPhoneNumber() != null &&
                            !updateProfileRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
                        Optional<User> existingUser = userRepository
                                .findByPhoneNumber(updateProfileRequest.getPhoneNumber());
                        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Phone number already exists");
                        }
                    }

                    // Cập nhật thông tin profile
                    if (updateProfileRequest.getEmail() != null) {
                        user.setEmail(updateProfileRequest.getEmail());
                    }
                    if (updateProfileRequest.getPhoneNumber() != null) {
                        user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
                    }
                    if (updateProfileRequest.getFullName() != null) {
                        user.setFullName(updateProfileRequest.getFullName());
                    }
                    if (updateProfileRequest.getAvatar() != null) {
                        user.setAvatar(updateProfileRequest.getAvatar());
                    }
                    if (updateProfileRequest.getDateOfBirth() != null) {
                        user.setDateOfBirth(updateProfileRequest.getDateOfBirth());
                    }
                    if (updateProfileRequest.getAddress() != null) {
                        user.setAddress(updateProfileRequest.getAddress());
                    }

                    // Lưu thay đổi
                    User savedUser = userRepository.save(user);

                    // Trả về thông tin user đã cập nhật
                    return new ProfileResponse(
                            savedUser.getUserId(),
                            savedUser.getUserName(),
                            savedUser.getEmail(),
                            savedUser.getPhoneNumber(),
                            savedUser.getFullName(),
                            savedUser.getAvatar(),
                            savedUser.getDateOfBirth(),
                            savedUser.getAddress(),
                            savedUser.getRoles().stream()
                                    .map(role -> role.getRoleName().name())
                                    .collect(Collectors.toList()),
                            savedUser.getCreatedAt(),
                            savedUser.getUpdatedAt());
                });
    }

    @Override
    public Optional<ProfileResponse> updateProfileWithAvatar(Integer userId, UpdateProfileRequest updateProfileRequest,
            MultipartFile avatarFile) throws IOException {
        return userRepository.findById(userId)
                .map(user -> {
                    try {
                        // Xóa avatar cũ nếu có
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            try {
                                String oldPublicId = cloudinaryService.extractPublicIdFromUrl(user.getAvatar());
                                if (oldPublicId != null) {
                                    cloudinaryService.deleteImage(oldPublicId);
                                }
                            } catch (Exception e) {
                                System.err.println("Warning: Failed to delete old avatar: " + e.getMessage());
                            }
                        }

                        // Upload avatar mới nếu có
                        if (avatarFile != null && !avatarFile.isEmpty()) {
                            String userName = user.getUserName();
                            String folderName = "avatars/" + userName;
                            String avatarUrl = cloudinaryService.uploadImage(avatarFile, folderName);
                            user.setAvatar(avatarUrl);
                        }

                        // Cập nhật các trường khác
                        if (updateProfileRequest.getEmail() != null) {
                            // Kiểm tra email trùng lặp
                            if (!updateProfileRequest.getEmail().equals(user.getEmail())) {
                                Optional<User> existingUser = userRepository
                                        .findByEmail(updateProfileRequest.getEmail());
                                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                                    throw new IllegalArgumentException("Email already exists");
                                }
                            }
                            user.setEmail(updateProfileRequest.getEmail());
                        }

                        if (updateProfileRequest.getPhoneNumber() != null) {
                            // Kiểm tra số điện thoại trùng lặp
                            if (!updateProfileRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
                                Optional<User> existingUser = userRepository
                                        .findByPhoneNumber(updateProfileRequest.getPhoneNumber());
                                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                                    throw new IllegalArgumentException("Phone number already exists");
                                }
                            }
                            user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
                        }

                        if (updateProfileRequest.getFullName() != null) {
                            user.setFullName(updateProfileRequest.getFullName());
                        }
                        if (updateProfileRequest.getDateOfBirth() != null) {
                            user.setDateOfBirth(updateProfileRequest.getDateOfBirth());
                        }
                        if (updateProfileRequest.getAddress() != null) {
                            user.setAddress(updateProfileRequest.getAddress());
                        }

                        // Lưu thay đổi
                        User savedUser = userRepository.save(user);

                        // Trả về thông tin user đã cập nhật
                        return new ProfileResponse(
                                savedUser.getUserId(),
                                savedUser.getUserName(),
                                savedUser.getEmail(),
                                savedUser.getPhoneNumber(),
                                savedUser.getFullName(),
                                savedUser.getAvatar(),
                                savedUser.getDateOfBirth(),
                                savedUser.getAddress(),
                                savedUser.getRoles().stream()
                                        .map(role -> role.getRoleName().name())
                                        .collect(Collectors.toList()),
                                savedUser.getCreatedAt(),
                                savedUser.getUpdatedAt());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload avatar", e);
                    }
                });
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AdminUserResponse> getUserDetailsForAdmin(Integer userId) {
        return userRepository.findById(userId)
                .map(this::mapToAdminUserResponse);
    }

    @Override
    public Optional<AdminUserResponse> updateUserStatus(Integer userId, UpdateUserStatusRequest request) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setStatus(request.getStatus());
                    User savedUser = userRepository.save(user);
                    return mapToAdminUserResponse(savedUser);
                });
    }

    @Override
    public List<AdminUserResponse> getUsersByStatus(String status) {
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            return userRepository.findAll().stream()
                    .filter(user -> user.getStatus() == accountStatus)
                    .map(this::mapToAdminUserResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFullName(),
                user.getAvatar(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getRoles().stream()
                        .map(role -> role.getRoleName().name())
                        .collect(Collectors.toList()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
