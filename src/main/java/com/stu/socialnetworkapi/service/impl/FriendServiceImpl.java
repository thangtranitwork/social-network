package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.neo4j.FriendRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
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

    @Override
    public List<UserCommonInformationResponse> getFriends(String username, Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        User target = userService.getUser(username);
        blockService.validateBlock(currentUserId, target.getId());
        return friendRepository.getFriends(target.getId(), pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
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
    public boolean isFriend(UUID user1, UUID user2) {
        return friendRepository.isFriend(user1, user2);
    }

    @Override
    public List<UserCommonInformationResponse> getSuggestedFriends(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return friendRepository.getSuggestedFriends(currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }

    @Override
    public List<UserCommonInformationResponse> getMutualFriends(String username, Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        UUID targetUserId = userService.getUser(username).getId();
        return friendRepository.getMutualFriends(currentUserId, targetUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }
}
