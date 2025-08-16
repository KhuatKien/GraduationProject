package com.phenikaa.userService.mapper;

import com.phenikaa.dto.response.UserInfoResponse;
import com.phenikaa.userService.dto.request.RegisterRequest;
import com.phenikaa.userService.dto.response.GetUserResponse;
import com.phenikaa.userService.entity.Role;
import com.phenikaa.userService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest registerRequest);

    GetUserResponse toDTO(User user);

    @Mapping(source = "roles",target = "roles", qualifiedByName = "mapRoleNames")
    UserInfoResponse toUserInfoResponse(User user);

    @Named("mapRoleNames")
    default List<String> mapRoleNames(Set<Role> roles) {
        if(roles == null) return null;
        return roles.stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toList());
    }
}
