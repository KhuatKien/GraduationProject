package com.phenikaa.userService.dto.request;

import com.phenikaa.userService.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "Status cannot be null")
    private AccountStatus status;
}
