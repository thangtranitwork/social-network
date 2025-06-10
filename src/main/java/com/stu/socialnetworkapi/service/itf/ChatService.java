package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.entity.User;

import java.util.List;

public interface ChatService {
    void createChatIfNotExist(User user1, User user2);

    List<ChatResponse> getChatList();

    List<ChatResponse> search(String query);

}
