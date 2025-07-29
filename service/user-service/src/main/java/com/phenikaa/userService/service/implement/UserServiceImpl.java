package com.phenikaa.userService.service.implement;

import com.phenikaa.dto.UserDto;
import com.phenikaa.userService.dao.implement.UserDaoImpl;
import com.phenikaa.userService.entity.User;
import com.phenikaa.userService.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDaoImpl userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public UserDto getUserById(Integer userId) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        return userDto;
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }
}
