package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.event.CommandEvent;
import com.stu.socialnetworkapi.event.TypingEvent;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.exception.WebSocketException;
import com.stu.socialnetworkapi.repository.redis.InChatRepository;
import com.stu.socialnetworkapi.repository.redis.IsOnlineRepository;
import com.stu.socialnetworkapi.repository.redis.IsTypingRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final IsOnlineRepository isOnlineRepository;
    private final IsTypingRepository isTypingRepository;
    private final InChatRepository inChatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_KEY = "userId";
    private static final String ROLE_KEY = "role";
    private static final String USER_ID_JWT_KEY = "sub";
    private static final String ROLE_JWT_KEY = "scope";
    private static final String SUBSCRIPTION_KEY = "subscription:";

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) throw new WebSocketException(ErrorCode.UNAUTHORIZED);
        String token = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(7);
        }
        StompCommand command = accessor.getCommand();
        Map<String, Object> attributes = Optional.ofNullable(accessor.getSessionAttributes())
                .orElse(new HashMap<>());

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(token, attributes, accessor);
        } else {
            String subcriptionId = accessor.getSubscriptionId();
            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                if (!authorizeSubscription(accessor)) {
                    throw new WebSocketException(ErrorCode.UNAUTHORIZED);
                }
                attributes.put(SUBSCRIPTION_KEY + subcriptionId, destination);
            } else if (StompCommand.DISCONNECT.equals(command)) {
                UUID userId = UUID.fromString(attributes.get(USER_ID_KEY).toString());
                isOnlineRepository.onUserDisconnected(userId);
            } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
                handleUnsubscribe(attributes, accessor);
            }

        }
        return message;
    }

    private void handleConnect(String token, Map<String, Object> attributes, StompHeaderAccessor accessor) {
        // Xác thực token và lưu thông tin user vào session
        Map<String, Object> claims = jwtUtil.validateToken(token);
        Object userId = claims.get(USER_ID_JWT_KEY);
        Object role = claims.get(ROLE_JWT_KEY);
        attributes.put(USER_ID_KEY, userId);
        attributes.put(ROLE_KEY, role);
        accessor.setUser(userId::toString);
        isOnlineRepository.onUserConnected(UUID.fromString(userId.toString()));
    }

    private void handleUnsubscribe(Map<String, Object> attributes, StompHeaderAccessor accessor) {
        UUID userId = UUID.fromString(attributes.get(USER_ID_KEY).toString());
        String subscriptionId = accessor.getSubscriptionId();
        String destination = (String) attributes.get(SUBSCRIPTION_KEY + subscriptionId);
        if (destination != null && destination.startsWith(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX)) {
            String chatId = destination.substring(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX.length() + 1);
            UUID chatUUID = UUID.fromString(chatId);
            UserTypingRequest request = new UserTypingRequest(chatUUID, userId, false);
            eventPublisher.publishEvent(new TypingEvent(this, request));
        } else if (destination != null && destination.startsWith(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX)) {
            String chatId = destination.substring(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX.length() + 1);
            UUID chatUUID = UUID.fromString(chatId);
            inChatRepository.unsubscribe(userId, chatUUID);
        }

        attributes.remove(SUBSCRIPTION_KEY + subscriptionId);
    }

    private boolean authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = Optional.ofNullable(accessor.getDestination())
                .orElseThrow(() -> new WebSocketException(ErrorCode.INVALID_WEBSOCKET_CHANNEL));
        Map<String, Object> attributes = Optional.ofNullable(accessor.getSessionAttributes())
                .orElseThrow(() -> new WebSocketException(ErrorCode.UNAUTHORIZED));
        String userId = Optional.ofNullable(attributes.get(USER_ID_KEY))
                .map(Object::toString)
                .orElseThrow(() -> new WebSocketException(ErrorCode.UNAUTHORIZED));
        if (destination.startsWith(WebSocketChannelPrefix.NOTIFICATION_CHANNEL_PREFIX)) {
            String userIdDestination = destination.substring(WebSocketChannelPrefix.NOTIFICATION_CHANNEL_PREFIX.length() + 1);
            return userIdDestination.equals(userId);
        }

        if (destination.startsWith(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX)) {
            return processSubscribeToChatChannel(destination, userId);
        }

        if (destination.startsWith(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX)) {
            String userIdDestination = destination.substring(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX.length() + 1);
            return userIdDestination.equals(userId);
        }

        if (destination.startsWith(WebSocketChannelPrefix.ONLINE_CHANNEL_PREFIX)) {
            String userIdDestination = destination.substring(WebSocketChannelPrefix.ONLINE_CHANNEL_PREFIX.length() + 1);
            return userIdDestination.equals(userId);
        }

        if (destination.startsWith(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX)) {
            return processSubscribeToTypingChannel(destination, userId);
        }

        if (destination.startsWith(WebSocketChannelPrefix.USER_WEBSOCKET_ERROR_CHANNEL_PREFIX)) {
            // Always success for receive error
            return true;
        }

        return false;
    }

    private boolean processSubscribeToTypingChannel(String destination, String userId) {
        String chatId = destination.substring(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX.length() + 1);
        UUID chatUUID = UUID.fromString(chatId);
        UUID userUUID = UUID.fromString(userId);
        boolean isMember = chatService.isMemberOfChat(userUUID, chatUUID);
        if (isMember) {
            UserTypingRequest request = new UserTypingRequest(chatUUID, userUUID, true);
            eventPublisher.publishEvent(new TypingEvent(this, request));
        }
        return isMember;
    }

    private boolean processSubscribeToChatChannel(String destination, String userId) {
        String chatId = destination.substring(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX.length() + 1);
        UUID chatUUID = UUID.fromString(chatId);
        UUID userUUID = UUID.fromString(userId);
        boolean isMember = chatService.isMemberOfChat(userUUID, chatUUID);
        if (isMember) {
            Set<String> typingUsers = isTypingRepository.getTypingUsersInChat(chatUUID);
            if (!typingUsers.isEmpty()) {
                for (String typingUser : typingUsers) {
                    UserTypingRequest request = new UserTypingRequest(chatUUID, UUID.fromString(typingUser), true);
                    eventPublisher.publishEvent(new TypingEvent(this, request));
                }
            }
        }
        inChatRepository.subscribe(userUUID, chatUUID);
        MessageCommand command = MessageCommand.builder()
                .command(MessageCommand.Command.READING)
                .id(String.valueOf(userUUID))
                .build();
        eventPublisher.publishEvent(new CommandEvent(command, chatUUID));
        return isMember;
    }

}