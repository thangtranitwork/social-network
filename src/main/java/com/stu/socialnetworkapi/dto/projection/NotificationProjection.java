package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;

import java.time.ZonedDateTime;
import java.util.UUID;

public record NotificationProjection (
        UUID id,
        NotificationAction action,
        ObjectType targetType,
        UUID targetId,
        ZonedDateTime sentAt,
        boolean isRead,
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId){
}
