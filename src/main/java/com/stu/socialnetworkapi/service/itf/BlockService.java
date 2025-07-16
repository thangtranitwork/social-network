package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.enums.BlockStatus;

import java.util.List;
import java.util.UUID;

public interface BlockService {

    void validateBlock(String username, String targetUsername);

    BlockStatus getBlockStatus(UUID blockerId, UUID targetId);

    BlockStatus getBlockStatus(String username, String targetUsername);

    void block(String username);

    void unblock(String username);

    List<UserCommonInformationResponse> getBlockedUsers();
}
