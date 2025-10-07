package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
import com.stu.socialnetworkapi.dto.projection.MessageProjection;
import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    private final UserMapper userMapper;

    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .content(message.getContent())
                .attachment(File.getPath(message.getAttachedFile()))
                .attachmentName(message.getAttachedFile() != null ? message.getAttachedFile().getName() : null)
                .sentAt(message.getSentAt())
                .sender(userMapper.toUserCommonInformationResponse(message.getSender()))
                .deleted(message.getDeleteAt() != null)
                .updated(message.getUpdateAt() != null)
                .isRead(message.isRead())
                .type(message.getType())
                .build();
    }

    public MessageResponse toMessageResponse(MessageProjection projection) {
        return MessageResponse.builder()
                .id(projection.id())
                .chatId(projection.chatId())
                .content(projection.content())
                .attachment(File.getPath(projection.attachmentId()))
                .attachmentName(projection.attachmentId() != null ? projection.attachmentName() : null)
                .sentAt(projection.sentAt())
                .sender(userMapper.toSenderUserCommonInformationResponse(projection))
                .deleted(projection.deleted())
                .updated(projection.updated())
                .type(projection.type())
                .callId(projection.callId())
                .callAt(projection.callAt())
                .answerAt(projection.answerAt())
                .endAt(projection.endAt())
                .isAnswered(projection.isAnswered())
                .isVideoCall(projection.isVideoCall())
                .isRead(projection.isRead())
                .build();
    }

    public MessageResponse toMessageResponse(final ChatProjection projection) {
        if (projection == null || projection.latestMessageId() == null) {
            return null;
        }
        return MessageResponse.builder()
                .chatId(projection.chatId())
                .id(projection.latestMessageId())
                .content(projection.latestMessageContent())
                .attachment(File.getPath(projection.latestMessageFileId()))
                .sentAt(projection.latestMessageSentAt())
                .deleted(projection.latestMessageDeleted())
                .sender(userMapper.toSenderUserCommonInformationResponse(projection))
                .type(projection.latestMessageType())
                .callId(projection.latestMessageCallId())
                .callAt(projection.latestMessageCallAt())
                .endAt(projection.latestMessageEndAt())
                .isAnswered(projection.latestMessageAnswered())
                .isVideoCall(projection.latestMessageIsVideoCall())
                .build();
    }
}
