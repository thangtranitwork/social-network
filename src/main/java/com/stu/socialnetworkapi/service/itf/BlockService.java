package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.BlockResponse;
import com.stu.socialnetworkapi.enums.BlockStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface BlockService {
    void validateBlock(UUID userId, UUID targetId);

    BlockStatus getBlockStatus(UUID blockerId, UUID targetId);

    void block(String username);

    void unblock(UUID blockId);

    Slice<BlockResponse> getBlockedUsers(Pageable pageable);
}
