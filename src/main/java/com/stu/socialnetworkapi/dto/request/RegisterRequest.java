package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Age;
import com.stu.socialnetworkapi.validation.annotation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        String email,
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Password
        String password,
        @NotBlank(message = "GIVEN_NAME_REQUIRED")
        String givenName,
        @NotBlank(message = "FAMILY_NAME_REQUIRED")
        String familyName,
        @NotNull(message = "BIRTHDATE_REQUIRED")
        @Age
        LocalDate birthdate
) {
}
