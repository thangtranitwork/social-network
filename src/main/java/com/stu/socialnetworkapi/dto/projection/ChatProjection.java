package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.ChatAction;
import com.stu.socialnetworkapi.enums.ChatType;
import com.stu.socialnetworkapi.enums.MessageType;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ChatProjection(
        UUID chatId,
        String name,
        ChatType chatType,
        String imageId,
        UUID latestMessageId,
        String latestMessageContent,
        MessageType latestMessageType,
        ZonedDateTime latestMessageSentAt,
        ChatAction latestMessageAction,
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
        int notReadMessageCount
) {
}
