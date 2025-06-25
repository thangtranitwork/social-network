package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.TextMessageRequest;
import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.service.itf.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final MessageService messageService;

    @MessageMapping("/chat")
    public MessageResponse sendMessage(@Payload TextMessageRequest text, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return messageService.sendMessage(text, userId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload UserTypingRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        messageService.typing(request, userId);
    }
}
