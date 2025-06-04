package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.GroupChatRequest;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.mapper.ChatMapper;
import com.stu.socialnetworkapi.repository.ChatRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;
    private final UserService userService;
    private final ChatRepository chatRepository;

    @Override
    public List<ChatResponse> getChatList() {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        return chatRepository.getChatListOrderByLatestMessageSentTimeDesc(userId)
                .stream().map(chatMapper::toChatResponse)
                .toList();
    }

    @Override
    public ChatResponse createGroupChat(GroupChatRequest request) {
        return null;
    }

    @Override
    public List<ChatResponse> search(String query) {
        UUID userId = userService.getCurrentUserIdRequiredAuthentication();
        return chatRepository.searchChats(userId, query)
                .stream().map(chatMapper::toChatResponse)
                .toList();
    }

    @Override
    public void addMember(UUID chatId, UUID newMemberId) {

    }

    @Override
    public void removeMember(UUID chatId, UUID memberId) {

    }

    @Override
    public void renameGroupChat(UUID chatId, String newName) {

    }

    @Override
    public void changeLeader(UUID chatId, UUID newLeaderId) {

    }

    @Override
    public void deleteChat(UUID chatId) {

    }

    @Override
    public void editChatImage(UUID chatId, MultipartFile newImage) {

    }

    @Override
    public void dissolveChat(UUID chatId) {

    }

    @Override
    public void leaveGroupChat(UUID chatId) {

    }
}
