package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandListener {
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleCommandEvent(CommandEvent event) {
        if (event.getChatId() == null) {
            log.debug("Chat id is null. Skipping command event...");
            return;
        }
        String destination = WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX + "/" + event.getChatId();
        log.debug("Sent to {} the command: {}", destination, event.getCommand());
        messagingTemplate.convertAndSend(destination, event.getCommand());

    }
}