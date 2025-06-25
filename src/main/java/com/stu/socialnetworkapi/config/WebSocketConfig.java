package com.stu.socialnetworkapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.stu.socialnetworkapi.config.WebSocketChannelPrefix.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${origin.front-end}")
    private String frontendOrigin;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(
                NOTIFICATION_CHANNEL_PREFIX,
                CHAT_CHANNEL_PREFIX,
                MESSAGE_CHANNEL_PREFIX,
                USER_WEBSOCKET_ERROR_CHANNEL_PREFIX,
                CALL_CHANNEL_PREFIX,
                ONLINE_CHANNEL_PREFIX);
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