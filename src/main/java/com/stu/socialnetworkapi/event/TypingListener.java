package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.service.itf.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypingListener {
    private final MessageService messageService;

    @Async
    @EventListener
    public void handleTypingEvent(TypingEvent event) {
        messageService.typing(event.getRequest());
    }
}