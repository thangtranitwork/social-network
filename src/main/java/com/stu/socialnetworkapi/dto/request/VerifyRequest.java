package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.Email;

import java.util.UUID;

public record VerifyRequest(
        @Email(message = "INVALID_EMAIL")
        String email,
        UUID code
) {
}
