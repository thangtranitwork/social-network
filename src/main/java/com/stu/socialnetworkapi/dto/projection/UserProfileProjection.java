package com.stu.socialnetworkapi.dto.projection;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record UserProfileProjection(
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String bio,
        LocalDate birthdate,
        String profilePictureId,
        String coverPictureId,
        int friendCount,
        int mutualFriendsCount,
        ZonedDateTime lastSeen,
        boolean isFriend,
        boolean showFriends,
        boolean allowFriendRequest
        ) {
}
