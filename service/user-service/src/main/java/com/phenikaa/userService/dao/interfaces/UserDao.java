package com.phenikaa.userService.dao.interfaces;

import com.phenikaa.userService.entity.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    User save(User user);
}
