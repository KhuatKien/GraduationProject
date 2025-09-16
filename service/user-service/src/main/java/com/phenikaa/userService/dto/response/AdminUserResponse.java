package com.phenikaa.userService.dto.response;

import com.phenikaa.userService.entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Integer userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String avatar;
    private LocalDate dateOfBirth;
    private String address;
    private List<String> roles;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
