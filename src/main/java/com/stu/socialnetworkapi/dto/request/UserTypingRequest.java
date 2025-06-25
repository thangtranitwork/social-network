package com.stu.socialnetworkapi.dto.request;

import java.util.UUID;

public record UserTypingRequest(
        UUID chatId,
        boolean isTyping
) {
}
