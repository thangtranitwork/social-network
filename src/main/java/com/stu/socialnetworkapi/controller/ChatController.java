package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.TextMessageRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<List<ChatResponse>> getChats() {
        return ApiResponse.success(chatService.getChatList());
    }

    @GetMapping("/messages/{chatId}")
    public ApiResponse<Slice<MessageResponse>> getMessages(@PathVariable UUID chatId, Pageable pageable) {
        return ApiResponse.success(messageService.getHistory(chatId, pageable));
    }

    @GetMapping("/search")
    public ApiResponse<List<ChatResponse>> searchChats(@RequestParam String query) {
        return ApiResponse.success(chatService.search(query));
    }

    @PostMapping("/send")
    public ApiResponse<MessageResponse> sendMessage(TextMessageRequest request) {
        return ApiResponse.success(messageService.sendMessage(request));
    }
}
