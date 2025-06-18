package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CommentProjection(
        UUID commentId,
        String content,
        int likeCount,
        int replyCount,
        String attachmentId,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        boolean liked,
        UUID authorId,
        String authorUsername,
        String authorGivenName,
        String authorFamilyName,
        String authorProfilePictureId,
        Boolean isFriend
) {
}
