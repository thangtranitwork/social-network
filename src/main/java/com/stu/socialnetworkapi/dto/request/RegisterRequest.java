package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.validation.annotation.Age;
import com.stu.socialnetworkapi.validation.annotation.OnlyLetter;
import com.stu.socialnetworkapi.validation.annotation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        String email,
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Password
        String password,
        @NotBlank(message = "GIVEN_NAME_REQUIRED")
        @Length(max = User.MAX_GIVEN_NAME_LENGTH, message = "INVALID_GIVEN_NAME_LENGTH")
        @OnlyLetter
        String givenName,
        @NotBlank(message = "FAMILY_NAME_REQUIRED")
        @Length(max = User.MAX_FAMILY_NAME_LENGTH, message = "INVALID_FAMILY_NAME_LENGTH")
        @OnlyLetter
        String familyName,
        @NotNull(message = "BIRTHDATE_REQUIRED")
        @Age
        LocalDate birthdate
) {
}
