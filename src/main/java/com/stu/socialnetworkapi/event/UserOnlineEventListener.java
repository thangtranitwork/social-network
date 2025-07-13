package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import com.stu.socialnetworkapi.repository.InChatRedisRepository;
import com.stu.socialnetworkapi.repository.IsTypingRedisRepository;
import com.stu.socialnetworkapi.repository.TargetChatIdRedisRepository;
import com.stu.socialnetworkapi.service.itf.CallService;
import com.stu.socialnetworkapi.service.itf.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOnlineEventListener {

    private final CallService callService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final InChatRedisRepository inChatRedisRepository;
    private final IsTypingRedisRepository isTypingRedisRepository;
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
        if (!event.isOnline()) {
            cleanUpTypingAndSubscribeChat(event.getUserId());
            callService.end(event.getUserId());
        }
    }

    private void cleanUpTypingAndSubscribeChat(UUID userId) {
        UUID chatId = isTypingRedisRepository.getChatId(userId);
        if (chatId != null) {
            UserTypingRequest request = new UserTypingRequest(chatId, userId, false);
            messageService.typing(request);
        }
        inChatRedisRepository.unsubscribeAll(userId);
    }

}