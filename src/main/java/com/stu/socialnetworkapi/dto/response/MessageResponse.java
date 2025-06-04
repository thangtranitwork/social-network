package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.ChatAction;
import com.stu.socialnetworkapi.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    UUID id;
    UUID chatId;
    String content;
    ZonedDateTime sentAt;
    MessageType type;
    ChatAction action;
    UserCommonInformationResponse sender;
    UserCommonInformationResponse target;
    String attachment;
}
