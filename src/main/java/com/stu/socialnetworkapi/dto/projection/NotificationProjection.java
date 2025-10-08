package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;

import java.time.ZonedDateTime;
import java.util.UUID;

public record NotificationProjection(
        UUID id,
        NotificationAction action,
        ObjectType targetType,
        UUID targetId,
        UUID postId,
        UUID commentId,
        UUID repliedCommentId,
        ZonedDateTime sentAt,
        boolean isRead,
        String shortenedContent,
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId
) {
}
