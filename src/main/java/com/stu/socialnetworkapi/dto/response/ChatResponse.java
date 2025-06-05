package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatResponse {
    UUID chatId;
    String name;
    MessageResponse latestMessage;
    UserCommonInformationResponse target;
    int notReadMessageCount;
}
