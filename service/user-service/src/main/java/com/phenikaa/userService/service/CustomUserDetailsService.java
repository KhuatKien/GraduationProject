package com.phenikaa.userService.service;

import com.phenikaa.userService.config.CustomUserDetails;
import com.phenikaa.userService.dao.interfaces.UserDao;
import com.phenikaa.userService.entity.Role;
import com.phenikaa.userService.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDao userDao;  // Inject UserDao để truy vấn thông tin người dùng từ CSDL

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .filter(Role::getIsActive)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(user, authorities);
    }
}