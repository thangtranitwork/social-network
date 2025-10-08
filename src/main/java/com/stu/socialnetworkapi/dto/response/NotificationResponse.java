package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    UUID id;
    NotificationAction action;
    ObjectType targetType;
    UUID targetId;
    UUID postId;
    UUID commentId;
    UUID repliedCommentId;
    String username;
    String shortenedContent;
    UserCommonInformationResponse creator;
    ZonedDateTime sentAt;
    boolean isRead;
}
