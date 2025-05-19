package com.stu.socialnetworkapi.config;

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

//@Component
//public class WebSocketAuthInterceptor implements HandshakeInterceptor {
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            String token = servletRequest.getServletRequest().getParameter("token");
//            jwtUtil.validateToken(token);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
//
//    }
//}
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Xác thực khi client CONNECT hoặc SUBSCRIBE
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header STOMP
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Xác thực token và lưu thông tin user vào session
            try {
                Map<String, Object> claims = jwtUtil.validateToken(token);
                accessor.getSessionAttributes().put("userId", claims.get("sub"));
                accessor.getSessionAttributes().put("role", claims.get("scope"));
            } catch (Exception e) {
                throw new IllegalArgumentException("Token không hợp lệ");
            }
        }
        // Quan trọng: Xác thực khi SUBSCRIBE
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // Lấy destination mà client muốn subscribe
            String destination = accessor.getDestination();

            // Lấy token từ header cho mỗi lần subscribe
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Xác thực token cho mỗi lần subscribe
            try {
                Map<String, Object> claims = jwtUtil.validateToken(token);

                // Kiểm tra quyền truy cập dựa trên destination và thông tin user
//                if (!authorizeSubscription(destination, claims)) {
//                    throw new IllegalArgumentException("Không có quyền subscribe vào " + destination);
//                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Xác thực thất bại khi subscribe: " + e.getMessage());
            }
        }

        return message;
    }

}