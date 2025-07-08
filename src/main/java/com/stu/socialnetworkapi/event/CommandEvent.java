package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.dto.response.MessageCommand;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class CommandEvent extends ApplicationEvent {
    private final MessageCommand command;
    private final UUID chatId;

    public CommandEvent(Object source, MessageCommand command, UUID chatId) {
        super(source);
        this.command = command;
        this.chatId = chatId;
    }
}
