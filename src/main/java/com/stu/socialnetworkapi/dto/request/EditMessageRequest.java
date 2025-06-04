package com.stu.socialnetworkapi.dto.request;

import java.util.UUID;

public record EditMessageRequest(
        UUID messagesId,
        String text
) {
}
