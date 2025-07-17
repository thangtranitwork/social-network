package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.enums.BlockStatus;

import java.util.List;

public interface BlockService {

    void validateBlock(String username, String targetUsername);

    BlockStatus getBlockStatus(String username, String targetUsername);

    void block(String username);

    void unblock(String username);

    List<UserCommonInformationResponse> getBlockedUsers();
}
