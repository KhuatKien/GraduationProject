package com.phenikaa.userService.mapper;

import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.dto.response.GetUserResponse;
import com.phenikaa.userService.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest registerRequest);

    GetUserResponse toDTO(User user);
}
