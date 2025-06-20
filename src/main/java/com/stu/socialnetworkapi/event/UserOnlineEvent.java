package com.stu.socialnetworkapi.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private final UUID userId;
    private final boolean isOnline;
    private final ZonedDateTime lastOnline;

    public UserOnlineEvent(Object source, UUID userId, boolean isOnline, ZonedDateTime lastOnline) {
        super(source);
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastOnline = lastOnline;
    }
}