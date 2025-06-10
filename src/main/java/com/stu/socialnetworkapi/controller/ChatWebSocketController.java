package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.TextMessageRequest;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.service.itf.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final MessageService messageService;

    @MessageMapping("/chat")
    public MessageResponse sendMessage(@Payload TextMessageRequest text, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(sessionAttributes.get("userId").toString());
        return messageService.sendMessage(text, userId);
    }
}
