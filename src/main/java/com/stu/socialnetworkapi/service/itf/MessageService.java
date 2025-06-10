package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.EditMessageRequest;
import com.stu.socialnetworkapi.dto.request.FileMessageRequest;
import com.stu.socialnetworkapi.dto.request.TextMessageRequest;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.util.UUID;

public interface MessageService {
    MessageResponse sendMessage(TextMessageRequest request);

    MessageResponse sendMessage(TextMessageRequest request, UUID userId);

    MessageResponse sendFile(FileMessageRequest message);

    List<MessageResponse> getHistory(UUID chatId, Pageable pageable);

    void editMessage(EditMessageRequest message);

    void deleteMessage(UUID messageId);

}
