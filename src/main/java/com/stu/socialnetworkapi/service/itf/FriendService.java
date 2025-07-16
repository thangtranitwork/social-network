package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;

import java.util.List;

public interface FriendService {
    List<UserCommonInformationResponse> getFriends(String username);

    void unfriend(String username);

    boolean isFriend(String user1, String user2);

    List<UserCommonInformationResponse> getSuggestedFriends();

    List<UserCommonInformationResponse> getMutualFriends(String username);
}
