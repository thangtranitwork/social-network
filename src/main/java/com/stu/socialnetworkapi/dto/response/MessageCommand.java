package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageCommand {
    UUID id;
    Command command;
    String message;
    Boolean isTyping;

    public enum Command {
        DELETE,
        EDIT,
        TYPING,
    }
}

