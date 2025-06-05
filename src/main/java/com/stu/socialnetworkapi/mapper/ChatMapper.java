package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    public ChatResponse toChatResponse(final ChatProjection projection) {
        if (projection == null) return null;
        return ChatResponse.builder()
                .chatId(projection.chatId())
                .name(projection.name())
                .latestMessage(messageMapper.toMessageResponse(projection))
                .target(userMapper.toTargetUserCommonInformationResponse(projection))
                .notReadMessageCount(projection.notReadMessageCount())
                .build();
    }
}
