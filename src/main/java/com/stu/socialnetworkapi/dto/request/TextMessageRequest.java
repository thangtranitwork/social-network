package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Username;
import jakarta.validation.constraints.NotBlank;

public record TextMessageRequest(
        @NotBlank(message = "MESSAGE_USERNAME_REQUIRED")
        @Username
        String username,
        @NotBlank(message = "TEXT_MESSAGE_CONTENT_REQUIRED")
        String text
) {
}
