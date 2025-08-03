package com.phenikaa.dto.response;

import java.util.List;

public record UserInfoResponse (Integer userId, String userName, List<String> roles) {}
