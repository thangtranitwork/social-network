package com.stu.socialnetworkapi.dto.projection;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminUserViewProjection(
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String bio,
        LocalDate birthdate,
        String profilePictureId,
        int friendCount,
        int postCount,
        int blockCount,
        int requestSentCount,
        int requestReceivedCount,
        int commentCount,
        int uploadedFileCount,
        int messageCount,
        int callCount,
        String email,
        ZonedDateTime registrationDate,
        boolean isVerified
) {
}