package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record FriendProjection(
        UUID friendId,
        ZonedDateTime createdAt,
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId) {
}