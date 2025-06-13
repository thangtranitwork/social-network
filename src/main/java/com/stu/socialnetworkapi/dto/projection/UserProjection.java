package com.stu.socialnetworkapi.dto.projection;

import java.util.UUID;

public record UserProjection(
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId,
        Integer mutualFriendsCount,
        Boolean isFriend
) {
}
