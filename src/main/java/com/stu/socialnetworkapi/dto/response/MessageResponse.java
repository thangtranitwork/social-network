package com.stu.socialnetworkapi.dto.response;

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
    UserCommonInformationResponse sender;
    String attachment;
    String attachmentName;
    boolean deleted;
    boolean updated;
}
