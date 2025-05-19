package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "INVALID_EMAIL")
        @NotBlank(message = "EMAIL_REQUIRED")
        String email,
        @Password
        @NotBlank(message = "PASSWORD_REQUIRED")
        String password
) {
}
