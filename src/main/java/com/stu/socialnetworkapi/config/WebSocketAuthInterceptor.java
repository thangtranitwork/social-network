package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.exception.WebSocketException;
import com.stu.socialnetworkapi.repository.ChatRepository;
import com.stu.socialnetworkapi.repository.IsOnlineRedisRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.MessageService;
import com.stu.socialnetworkapi.util.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final IsOnlineRedisRepository isOnlineRedisRepository;
    private final MessageService messageService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_KEY = "userId";
    private static final String ROLE_KEY = "role";
    private static final String USER_ID_JWT_KEY = "sub";
    private static final String ROLE_JWT_KEY = "scope";

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }
        String token = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(7);
        }
        StompCommand command = accessor.getCommand();
        Map<String, Object> attributes = Optional.ofNullable(accessor.getSessionAttributes())
                .orElse(new HashMap<>());

        if (StompCommand.CONNECT.equals(command)) {
            // Xác thực token và lưu thông tin user vào session
            Map<String, Object> claims = jwtUtil.validateToken(token);
            Object userId = claims.get(USER_ID_JWT_KEY);
            Object role = claims.get(ROLE_JWT_KEY);
            attributes.put(USER_ID_KEY, userId);
            attributes.put(ROLE_KEY, role);
            accessor.setUser(userId::toString);
            log.info("User {} connecting", userId);
            isOnlineRedisRepository.onUserConnected(UUID.fromString(userId.toString()));
        }
        //Xác thực khi SUBSCRIBE
        else {
            String destination = accessor.getDestination();
            if (StompCommand.SUBSCRIBE.equals(command)) {
                log.info("User {} subscribed to channel {}", attributes.get(USER_ID_KEY), destination);
                if (!authorizeSubscription(accessor)) {
                    throw new WebSocketException(ErrorCode.UNAUTHORIZED);
                }
            } else if (StompCommand.DISCONNECT.equals(command)) {
                UUID userId = UUID.fromString(attributes.get(USER_ID_KEY).toString());
                isOnlineRedisRepository.onUserDisconnected(userId);
            } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
                UUID userId = UUID.fromString(attributes.get(USER_ID_KEY).toString());
                if (destination.startsWith(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX)) {
                    String chatId = destination.substring(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX.length() + 1);
                    UUID chatUUID = UUID.fromString(chatId);
                    UUID userUUID = UUID.fromString(userId.toString());
                    messageService.typing(new UserTypingRequest(chatUUID, userUUID, false));
                }
                log.info("User {} unsubscribed from channel {}", userId, destination);
            }
        }
        return message;
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
            String chatId = destination.substring(WebSocketChannelPrefix.CHAT_CHANNEL_PREFIX.length() + 1);
            return chatService.isMemberOfChat(UUID.fromString(userId), UUID.fromString(chatId));
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
            String chatId = destination.substring(WebSocketChannelPrefix.TYPING_CHANNEL_PREFIX.length() + 1);
            UUID chatUUID = UUID.fromString(chatId);
            UUID userUUID = UUID.fromString(userId);
            boolean authenticated = chatService.isMemberOfChat(userUUID, chatUUID);
            if (authenticated) {
                messageService.typing(new UserTypingRequest(chatUUID, userUUID, true));
            }
            return authenticated;
        }

        if (destination.startsWith(WebSocketChannelPrefix.USER_WEBSOCKET_ERROR_CHANNEL_PREFIX)) {
            // Always success for receive error
            return true;
        }

        return false;
    }

}