package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import com.stu.socialnetworkapi.repository.TargetChatIdRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOnlineEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final TargetChatIdRedisRepository targetChatIdRedisRepository;

    @Async
    @EventListener
    public void handleUserOnlineEvent(UserOnlineEvent event) {
        OnlineResponse response = OnlineResponse.builder()
                .userId(event.getUserId())
                .isOnline(event.isOnline())
                .lastOnline(event.getLastOnline())
                .build();

        targetChatIdRedisRepository.getTargetChatIds(event.getUserId())
                .forEach(id ->
                        messagingTemplate.convertAndSend(
                                WebSocketChannelPrefix.ONLINE_CHANNEL_PREFIX + "/" + id,
                                response
                        )
                );
    }
}