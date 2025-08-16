package com.phenikaa.userService.repository;

import com.phenikaa.userService.entity.Role;
import com.phenikaa.userService.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);
}
