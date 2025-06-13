package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.RequestDirection;

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
        int friendCount,
        int mutualFriendsCount,
        ZonedDateTime lastSeen,
        boolean isFriend,
        RequestDirection request,
        BlockStatus blockStatus,
        int postCount
) {
}
