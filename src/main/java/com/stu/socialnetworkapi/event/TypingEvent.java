package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TypingEvent extends ApplicationEvent {
    private transient final UserTypingRequest request;

    public TypingEvent(Object source, UserTypingRequest request) {
        super(source);
        this.request = request;
    }
}
