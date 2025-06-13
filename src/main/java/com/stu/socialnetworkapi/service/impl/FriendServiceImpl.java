package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.FriendResponse;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.FriendMapper;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.FriendRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.FriendService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final BlockService blockService;
    private final FriendMapper friendMapper;
    private final FriendRepository friendRepository;

    @Override
    public List<FriendResponse> getFriends(String username ,Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        User target = userService.getUser(username);
        blockService.validateBlock(currentUserId, target.getId());
        return friendRepository.getFriends(target.getId(), pageable).stream()
                .map(friendMapper::toFriendResponse)
                .toList();
    }

    @Override
    public void unfriend(UUID friendId) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        if (!friendRepository.canUnfriend(friendId, currentUserId)) {
            throw new ApiException(ErrorCode.FRIEND_NOT_FOUND);
        }
        friendRepository.unfriend(friendId);
    }

    @Override
    public boolean isFriend(UUID user1, UUID user2) {
        return friendRepository.isFriend(user1, user2);
    }

    @Override
    public List<UserCommonInformationResponse> getSuggestedFriends(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return friendRepository.getSuggestedFriends(currentUserId, pageable).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }

    @Override
    public List<UserCommonInformationResponse> getMutualFriends(String username, Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        UUID targetUserId = userService.getUser(username).getId();
        return friendRepository.getMutualFriends(currentUserId, targetUserId, pageable).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }
}
