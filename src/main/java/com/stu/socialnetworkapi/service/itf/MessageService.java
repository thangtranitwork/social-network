package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.EditMessageRequest;
import com.stu.socialnetworkapi.dto.request.FileMessageRequest;
import com.stu.socialnetworkapi.dto.request.TextMessageRequest;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface MessageService {
    MessageResponse sendMessage(TextMessageRequest message);

    MessageResponse sendFile(FileMessageRequest message);

    Slice<MessageResponse> getHistory(UUID chatId, Pageable pageable);

    void editMessage(EditMessageRequest message);

    void deleteMessage(UUID messageId);

}
