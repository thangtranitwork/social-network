package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
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
                .attachmentName(message.getAttachedFile().getName())
                .sentAt(message.getSentAt())
                .sender(userMapper.toUserCommonInformationResponse(message.getSender()))
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
                .sender(userMapper.toSenderUserCommonInformationResponse(projection))
                .build();
    }
}
