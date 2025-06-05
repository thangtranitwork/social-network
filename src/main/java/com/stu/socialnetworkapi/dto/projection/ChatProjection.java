package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.BlockStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ChatProjection(
        UUID chatId,
        String name,
        UUID latestMessageId,
        String latestMessageContent,
        String latestMessageFileId,
        ZonedDateTime latestMessageSentAt,
        UUID latestMessageSenderId,
        String latestMessageSenderUsername,
        String latestMessageSenderGivenName,
        String latestMessageSenderFamilyName,
        String latestMessageSenderProfilePictureId,
        UUID targetId,
        String targetUsername,
        String targetGivenName,
        String targetFamilyName,
        String targetProfilePictureId,
        int notReadMessageCount,
        boolean isFriend,
        BlockStatus blockStatus
) {
}
