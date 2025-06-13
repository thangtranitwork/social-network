package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

import java.util.UUID;

public interface FriendService {
    List<UserCommonInformationResponse> getFriends(String username , Pageable pageable);

    void unfriend(String username);

    boolean isFriend(UUID user1, UUID user2);

    List<UserCommonInformationResponse> getSuggestedFriends(Pageable pageable);

    List<UserCommonInformationResponse> getMutualFriends(String username, Pageable pageable);
}
