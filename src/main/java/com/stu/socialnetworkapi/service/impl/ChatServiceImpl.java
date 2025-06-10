package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.mapper.ChatMapper;
import com.stu.socialnetworkapi.repository.ChatRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;
    private final UserService userService;
    private final ChatRepository chatRepository;

    @Override
    public void createChatIfNotExist(User user1, User user2) {
        Optional<UUID> optional = chatRepository.getDirectChatIdByMemberIds(user1.getId(), user2.getId());
        if (optional.isEmpty()) {
            chatRepository.save(Chat.builder()
                    .members(List.of(user1, user2))
                    .build());
        }
    }

    @Override
    public List<ChatResponse> getChatList() {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        return chatRepository.getChatListOrderByLatestMessageSentTimeDesc(userId)
                .stream().map(chatMapper::toChatResponse)
                .toList();
    }

    @Override
    public List<ChatResponse> search(String query) {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        return chatRepository.searchChats(userId, query)
                .stream().map(chatMapper::toChatResponse)
                .toList();
    }
}
