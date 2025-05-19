package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest (
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        String email,
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Password
        String password
){
}
