package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageCommand {
    String id;
    Command command;
    String message;

    public enum Command {
        DELETE,
        EDIT,
        TYPING,
        STOP_TYPING,
        HAS_BEEN_BLOCKED,
        HAS_BEEN_UNBLOCKED,
        READING,
        END_CALL
    }
}

