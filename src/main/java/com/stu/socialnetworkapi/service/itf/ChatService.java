package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.entity.Chat;
import com.stu.socialnetworkapi.entity.User;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    void createChatIfNotExist(User user1, User user2);

    Chat getOrCreateDirectChat(UUID senderId, UUID receiverId);

    Chat getOrCreateDirectChat(User sender, User receiver);

    List<ChatResponse> getChatList();

    List<ChatResponse> search(String query);
}
