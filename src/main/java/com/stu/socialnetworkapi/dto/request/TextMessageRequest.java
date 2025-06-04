package com.stu.socialnetworkapi.dto.request;

import java.util.UUID;

public record TextMessageRequest(
        UUID chatId,
        UUID userId,
        String text
) {
}
