package com.stu.socialnetworkapi.dto.projection;

import java.util.UUID;

public record BlockProjection(
        UUID blockId,
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId
) {
}
