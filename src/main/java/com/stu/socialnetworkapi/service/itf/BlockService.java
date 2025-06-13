package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.enums.BlockStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BlockService {
    void validateBlock(UUID userId, UUID targetId);

    BlockStatus getBlockStatus(UUID blockerId, UUID targetId);

    void block(String username);

    void unblock(String username);

    List<UserCommonInformationResponse> getBlockedUsers(Pageable pageable);
}
