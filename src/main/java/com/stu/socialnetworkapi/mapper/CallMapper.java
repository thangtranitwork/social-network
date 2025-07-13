package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.response.MessageResponse;
import com.stu.socialnetworkapi.entity.Call;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallMapper {
    private final UserMapper userMapper;

    public MessageResponse toMessageResponse(Call call) {
        return MessageResponse.builder()
                .id(call.getId())
                .sentAt(call.getSentAt())
                .callId(call.getCallId())
                .sender(userMapper.toUserCommonInformationResponse(call.getSender()))
                .isVideoCall(call.isVideoCall())
                .isAnswered(call.isAnswered())
                .isRead(call.isRead())
                .isAnswered(call.isAnswered())
                .build();
    }
}
