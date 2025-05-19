package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.BlockProjection;
import com.stu.socialnetworkapi.dto.response.BlockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockMapper {
    private final UserMapper userMapper;

    public BlockResponse toBlockResponse(final BlockProjection projection) {
        return BlockResponse.builder()
                .blockId(projection.blockId())
                .user(userMapper.toUserCommonInformationResponse(projection))
                .build();
    }
}
