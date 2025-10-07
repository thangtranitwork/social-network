package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.*;
import com.stu.socialnetworkapi.dto.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponse sendMessage(TextMessageRequest request);

    MessageResponse sendMessage(TextMessageRequest request, UUID userId);

    MessageResponse sendFile(FileMessageRequest message);

    MessageResponse sendGif(GifMessageRequest message);

    List<MessageResponse> getHistory(UUID chatId, Neo4jPageable pageable);

    void editMessage(EditMessageRequest message);

    void deleteMessage(UUID messageId);

    void typing(UserTypingRequest request);
}
