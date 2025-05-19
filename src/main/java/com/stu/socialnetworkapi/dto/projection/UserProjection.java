package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record UserProjection(
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId,
        int mutualFriendsCount,
        ZonedDateTime lastSeen,
        boolean isFriend
) {
}
