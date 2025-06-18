package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.MessageType;

import java.time.ZonedDateTime;
import java.util.UUID;

public record MessageProjection(
        UUID id,
        UUID chatId,
        String content,
        ZonedDateTime sentAt,
        UUID senderId,
        String senderUsername,
        String senderGivenName,
        String senderFamilyName,
        String senderProfilePictureId,
        String attachmentId,
        String attachmentName,
        boolean deleted,
        boolean updated,
        MessageType type,
        String callId,
        ZonedDateTime callAt,
        ZonedDateTime answerAt,
        ZonedDateTime endAt,
        boolean isAnswered,
        boolean isVideoCall
) {
}
