package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.FriendResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface FriendService {
    Slice<FriendResponse> getFriends(Pageable pageable);
    void unfriend(UUID friendId);
    boolean isFriend(UUID user1,UUID user2);
    Slice<UserCommonInformationResponse> getSuggestedFriends(Pageable pageable);
    Slice<UserCommonInformationResponse> getMutualFriends(String username, Pageable pageable);
}
