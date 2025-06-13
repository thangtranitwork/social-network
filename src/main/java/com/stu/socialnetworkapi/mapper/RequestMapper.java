package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.RequestProjection;
import com.stu.socialnetworkapi.dto.response.RequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestMapper {
    private final UserMapper userMapper;

    public RequestResponse toRequestResponse(final RequestProjection projection) {
        return RequestResponse.builder()
                .uuid(projection.requestId())
                .sentAt(projection.sentAt())
                .user(userMapper.toUserCommonInformationResponse(projection))
                .build();
    }
}
