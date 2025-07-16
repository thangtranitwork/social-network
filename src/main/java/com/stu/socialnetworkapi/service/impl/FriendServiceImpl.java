package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.neo4j.FriendRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import com.stu.socialnetworkapi.repository.redis.RelationshipCacheRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.FriendService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final BlockService blockService;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final RelationshipCacheRepository relationshipCacheRepository;

    @Override
    public List<UserCommonInformationResponse> getFriends(String username) {
        String currentUsername = userService.getCurrentUsernameRequiredAuthentication();
        User target = userService.getUser(username);
        blockService.validateBlock(currentUsername, target.getUsername());
        return relationshipCacheRepository.getFriend(username);
    }

    @Override
    public void unfriend(String username) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        User target = userService.getUser(username);
        UUID uuid = friendRepository.getFriendId(currentUserId, target.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.FRIEND_NOT_FOUND));
        friendRepository.deleteByUuid(uuid);
        userRepository.recalculateUserCounters(currentUserId);
        userRepository.recalculateUserCounters(target.getId());
    }

    @Override
    public boolean isFriend(String user1, String user2) {
        return relationshipCacheRepository.isFriend(user1, user2);
    }

    @Override
    public List<UserCommonInformationResponse> getSuggestedFriends() {
        String username = userService.getCurrentUsernameRequiredAuthentication();
        return relationshipCacheRepository.getSuggestedFriends(username);
    }

    @Override
    public List<UserCommonInformationResponse> getMutualFriends(String username) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        UUID targetUserId = userService.getUser(username).getId();
        return friendRepository.getMutualFriends(currentUserId, targetUserId, User.MAX_FRIEND_COUNT).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }
}
