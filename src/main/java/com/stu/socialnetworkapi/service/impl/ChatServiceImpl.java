package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.ChatMapper;
import com.stu.socialnetworkapi.repository.ChatRepository;
import com.stu.socialnetworkapi.repository.InChatRedisRepository;
import com.stu.socialnetworkapi.repository.TargetChatIdRedisRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
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
    private final BlockService blockService;
    private final ChatRepository chatRepository;
    private final InChatRedisRepository inChatRedisRepository;
    private final TargetChatIdRedisRepository targetChatIdRedisRepository;

    @Override
    public void createChatIfNotExist(User user1, User user2) {
        Optional<UUID> optional = chatRepository.getDirectChatIdByMemberIds(user1.getId(), user2.getId());
        if (optional.isEmpty()) {
            chatRepository.save(Chat.builder()
                    .members(List.of(user1, user2))
                    .build());
            inChatRedisRepository.invalidateUserChat(user1.getId());
            inChatRedisRepository.invalidateUserChat(user2.getId());
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
        inChatRedisRepository.invalidateUserChat(sender.getId());
        inChatRedisRepository.invalidateUserChat(receiver.getId());
        targetChatIdRedisRepository.invalidate(sender.getId());
        targetChatIdRedisRepository.invalidate(receiver.getId());
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
        return inChatRedisRepository.isInChat(userId, chatId);
    }
}
