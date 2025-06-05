package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record EditMessageRequest(
        UUID messagesId,
        @NotBlank(message = "TEXT_MESSAGE_CONTENT_REQUIRED")
        String text
) {
}
