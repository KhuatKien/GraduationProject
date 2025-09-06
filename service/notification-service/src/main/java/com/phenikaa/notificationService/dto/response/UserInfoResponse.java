package com.phenikaa.notificationService.dto.response;

import java.util.List;

public record UserInfoResponse(Integer userId, String userName, String email, String phoneNumber, List<String> roles) {
}
