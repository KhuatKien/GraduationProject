package com.phenikaa.userService.service.implement;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.entity.AccountStatus;
import com.phenikaa.userService.entity.Role;
import com.phenikaa.userService.entity.RoleName;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.mapper.UserMapper;
import com.phenikaa.userService.repository.RoleRepository;
import com.phenikaa.userService.repository.UserRepository;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public User save(RegisterRequest registerRequest) {
        Optional<User> userExist = userRepository.findByUserNameOrEmail(registerRequest.getUserName(), registerRequest.getEmail()) ;
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
                                .collect(Collectors.toList())
                ));
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
                                .collect(Collectors.toList())
                ));
    }
}
