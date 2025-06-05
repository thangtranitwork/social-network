package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getChatList();

    List<ChatResponse> search(String query);

}
