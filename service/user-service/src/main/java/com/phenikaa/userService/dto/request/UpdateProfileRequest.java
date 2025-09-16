package com.phenikaa.userService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;

    @Size(max = 255, message = "Full name must be less than 255 characters")
    private String fullName;

    @Size(max = 255, message = "Avatar URL must be less than 255 characters")
    private String avatar;

    private LocalDate dateOfBirth;

    @Size(max = 1000, message = "Address must be less than 1000 characters")
    private String address;
}
