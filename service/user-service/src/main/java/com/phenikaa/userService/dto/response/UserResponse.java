package com.phenikaa.userService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String avatar;
    private String address;
    private String status;
}
