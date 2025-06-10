package com.stu.socialnetworkapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${front-end.origin}")
    private String frontendOrigin;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public static final String NOTIFICATION_CHANNEL_PREFIX = "/notifications"; // /notifications/{userid} Realtime notification
    public static final String CHAT_CHANNEL_PREFIX = "/chat"; // /chat/{chatid} Receive new message and chat info state when chat is on screen
    public static final String CHAT_COMMAND_CHANNEL_PREFIX = "/chat-command"; // /chat-command/{chatid} Receive message command like delete, edit, ... when chat is on screen
    public static final String MESSAGE_CHANNEL_PREFIX = "/message"; // /message/{userid} Received new message for notification (chat is not on screen)
    public static final String USER_WEBSOCKET_ERROR_CHANNEL_PREFIX = "/errors"; // Send websocket error

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(
                NOTIFICATION_CHANNEL_PREFIX,
                CHAT_CHANNEL_PREFIX,
                CHAT_COMMAND_CHANNEL_PREFIX,
                MESSAGE_CHANNEL_PREFIX,
                USER_WEBSOCKET_ERROR_CHANNEL_PREFIX);
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin);
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}