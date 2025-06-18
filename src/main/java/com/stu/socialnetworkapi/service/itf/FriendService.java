package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    List<UserCommonInformationResponse> getFriends(String username, Neo4jPageable pageable);

    void unfriend(String username);

    boolean isFriend(UUID user1, UUID user2);

    List<UserCommonInformationResponse> getSuggestedFriends(Neo4jPageable pageable);

    List<UserCommonInformationResponse> getMutualFriends(String username, Neo4jPageable pageable);
}
