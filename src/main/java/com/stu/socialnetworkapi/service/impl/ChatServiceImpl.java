package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.ChatMapper;
import com.stu.socialnetworkapi.repository.neo4j.ChatRepository;
import com.stu.socialnetworkapi.repository.redis.InChatRepository;
import com.stu.socialnetworkapi.repository.redis.TargetChatIdRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;
    private final UserService userService;
    private final BlockService blockService;
    private final ChatRepository chatRepository;
    private final InChatRepository inChatRepository;
    private final TargetChatIdRepository targetChatIdRepository;

    @Override
    public void createChatIfNotExist(User user1, User user2) {
        Optional<UUID> optional = chatRepository.getDirectChatIdByMemberIds(user1.getId(), user2.getId());
        if (optional.isEmpty()) {
            chatRepository.save(Chat.builder()
                    .members(List.of(user1, user2))
                    .build());
            inChatRepository.invalidateUserChat(user1.getId());
            inChatRepository.invalidateUserChat(user2.getId());
        }
    }

    @Override
    public Chat getOrCreateDirectChat(UUID senderId, UUID receiverId) {
        return getOrCreateDirectChat(userService.getUser(senderId), userService.getUser(receiverId));
    }

    @Override
    public Chat getOrCreateDirectChat(User sender, User receiver) {
        blockService.validateBlock(sender.getId(), receiver.getId());

        UUID existingChatId = chatRepository.getDirectChatIdByMemberIds(sender.getId(), receiver.getId())
                .orElse(null);

        if (existingChatId != null) {
            return chatRepository.findById(existingChatId)
                    .orElseThrow(() -> new ApiException(ErrorCode.CHAT_NOT_FOUND));
        }

        List<User> members = List.of(sender, receiver);

        Chat newChat = Chat.builder()
                .members(members)
                .build();
        chatRepository.save(newChat);
        inChatRepository.invalidateUserChat(sender.getId());
        inChatRepository.invalidateUserChat(receiver.getId());
        targetChatIdRepository.invalidate(sender.getId());
        targetChatIdRepository.invalidate(receiver.getId());
        return newChat;
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

    @Override
    public boolean isMemberOfChat(UUID userId, UUID chatId) {
        return inChatRepository.isInChat(userId, chatId);
    }
}
