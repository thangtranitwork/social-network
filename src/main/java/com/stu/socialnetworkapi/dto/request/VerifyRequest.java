package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VerifyRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        String email,
        @NotNull(message = "VERIFICATION_CODE_REQUIRED")
        UUID code
) {
}
