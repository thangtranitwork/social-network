package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.FriendProjection;
import com.stu.socialnetworkapi.dto.response.FriendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendMapper {
    private final UserMapper userMapper;

    public FriendResponse toFriendResponse(final FriendProjection projection) {
        return FriendResponse.builder()
                .friendId(projection.friendId())
                .createdAt(projection.createdAt())
                .user(userMapper.toUserCommonInformationResponse(projection))
                .build();
    }
}
