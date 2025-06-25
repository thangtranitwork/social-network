package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MessageCommand {
    UUID id;
    Command command;
    String message;

    public enum Command {
        DELETE,
        EDIT,
        TYPING,
    }
}

