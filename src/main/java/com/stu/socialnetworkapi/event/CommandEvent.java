package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.dto.response.MessageCommand;

import java.util.UUID;

public record CommandEvent(MessageCommand command, UUID chatId) {
}
