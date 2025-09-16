package com.phenikaa.bookingService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Integer userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String fullName;
}
