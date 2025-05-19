package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record RequestProjection(
        UUID requestId,
        ZonedDateTime sentAt,
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId) {
}
