package com.phenikaa.userService.mapper;

import com.phenikaa.userService.dto.request.CreateUserRequest;
import com.phenikaa.userService.dto.response.GetUserResponse;
import com.phenikaa.userService.entity.User;
import jakarta.persistence.EntityManager;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(CreateUserRequest createUserRequest, @Context EntityManager entityManager);

    GetUserResponse toDTO(User user);
}
