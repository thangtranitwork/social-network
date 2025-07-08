package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandListener {
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleCommandEvent(CommandEvent event) {
        messagingTemplate.convertAndSend(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + event.getChatId(), event.getCommand());
    }
}