package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.PostPrivacy;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record PostProjection(
        UUID id,
        String content,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        PostPrivacy privacy,
        List<String> files,
        int likeCount,
        int shareCount,
        int commentCount,
        boolean liked,
        UUID authorId,
        String authorUsername,
        String authorGivenName,
        String authorFamilyName,
        String authorProfilePictureId,
        boolean isFriend,
        UUID originalPostId,
        String originalPostContent,
        ZonedDateTime originalPostCreatedAt,
        ZonedDateTime originalPostUpdatedAt,
        PostPrivacy originalPostPrivacy,
        List<String> originalPostFiles,
        UUID originalPostAuthorId,
        String originalPostAuthorUsername,
        String originalPostAuthorGivenName,
        String originalPostAuthorFamilyName,
        String originalPostAuthorProfilePictureId,
        boolean isSharedPost,
        boolean originalPostCanView,
        Double score
) {
}
