package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.GroupChatRequest;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatResponse> getChatList();

    ChatResponse createGroupChat(GroupChatRequest request);

    List<ChatResponse> search(String query);

    void addMember(UUID chatId, UUID newMemberId);

    void removeMember(UUID chatId, UUID memberId);

    void renameGroupChat(UUID chatId, String newName);

    void changeLeader(UUID chatId, UUID newLeaderId);

    void deleteChat(UUID chatId);

    void editChatImage(UUID chatId, MultipartFile newImage);

    void dissolveChat(UUID chatId);

    void leaveGroupChat(UUID chatId);
}
