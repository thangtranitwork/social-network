package com.stu.socialnetworkapi.dto.request;

import java.util.UUID;

public record UserTypingRequest(
        UUID chatId,
        UUID userId,
        boolean isTyping
) {
}
