package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.ChatType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatResponse {
    UUID chatId;
    String name;
    ChatType type;
    MessageResponse latestMessage;
    UserCommonInformationResponse target;
    int notReadMessageCount;
}
