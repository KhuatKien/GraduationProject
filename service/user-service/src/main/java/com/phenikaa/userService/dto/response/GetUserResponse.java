package com.phenikaa.userService.dto.response;

import com.phenikaa.userService.entity.AccountStatus;
import lombok.Data;

import java.util.List;

@Data
public class GetUserResponse {
    private Integer userId;
    private String fullName;
    private String email;
    private AccountStatus status;
    private List<Integer> roleIds;
}
