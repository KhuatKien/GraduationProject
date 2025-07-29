package com.phenikaa.userService.service.interfaces;

import com.phenikaa.dto.UserDto;
import com.phenikaa.userService.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    UserDto getUserById(Integer userId);
    User save(User user);
}
