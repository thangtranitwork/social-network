package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.IsOnlineRedisRepository;
import com.stu.socialnetworkapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private IsOnlineRedisRepository isOnlineRedisRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            // Xác thực token và lưu thông tin user vào session
            try {
                Map<String, Object> claims = jwtUtil.validateToken(token);
                accessor.getSessionAttributes().put("userId", claims.get("sub"));
                accessor.getSessionAttributes().put("role", claims.get("scope"));
                isOnlineRedisRepository.save(UUID.fromString(claims.get("sub").toString()), true);
            } catch (Exception e) {
                throw new IllegalArgumentException("Token không hợp lệ");
            }
        }
        // Quan trọng: Xác thực khi SUBSCRIBE
        else if (StompCommand.SUBSCRIBE.equals(command)) {
            // Lấy destination mà client muốn subscribe
            String destination = accessor.getDestination();

            // Xác thực token cho mỗi lần subscribe
            try {
                Map<String, Object> claims = jwtUtil.validateToken(token);

                if (!authorizeSubscription(Objects.requireNonNull(destination), claims, accessor)) {
                    throw new ApiException(ErrorCode.UNAUTHORIZED);
                }
            } catch (Exception e) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
        } else if (StompCommand.DISCONNECT.equals(command)) {
            UUID userId = UUID.fromString(accessor.getSessionAttributes().get("userId").toString());
            isOnlineRedisRepository.save(userId, false);
        }

        return message;
    }

    private boolean authorizeSubscription(String destination, Map<String, Object> claims, StompHeaderAccessor accessor) {
        String userId = Optional.ofNullable(accessor.getSessionAttributes().get("userId"))
                .map(Object::toString)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        if (destination.startsWith(WebSocketConfig.NOTIFICATION_CHANNEL_PREFIX)) {
            String userIdDestination = destination.substring(WebSocketConfig.NOTIFICATION_CHANNEL_PREFIX.length() + 1);
            return userIdDestination.equals(userId);
        }
        if (destination.startsWith(WebSocketConfig.CHAT_CHANNEL_PREFIX)) {

        }
        return false;
    }

}