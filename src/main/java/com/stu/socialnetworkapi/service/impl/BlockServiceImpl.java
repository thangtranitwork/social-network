package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.event.CommandEvent;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.BlockRepository;
import com.stu.socialnetworkapi.repository.redis.InChatRepository;
import com.stu.socialnetworkapi.repository.redis.RelationshipCacheRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final InChatRepository inChatRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RelationshipCacheRepository relationshipCacheRepository;

    @Override
    public void validateBlock(String username, String targetUsername) {
        if (relationshipCacheRepository.isBlocked(username, targetUsername))
            throw new ApiException(ErrorCode.HAS_BLOCKED);
        else if (relationshipCacheRepository.isBlocked(targetUsername, username))
            throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
    }

    @Override
    public BlockStatus getBlockStatus(String username, String targetUsername) {
        if (relationshipCacheRepository.isBlocked(username, targetUsername))
            return BlockStatus.BLOCKED;
        else if (relationshipCacheRepository.isBlocked(targetUsername, username))
            return BlockStatus.HAS_BEEN_BLOCKED;
        else
            return BlockStatus.NORMAL;
    }

    @Override
    public void block(String username) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        String currentUsername = currentUser.getUsername();

        User targetUser = userService.getUser(username);

        if (currentUsername.equals(username)) {
            throw new ApiException(ErrorCode.CAN_NOT_BLOCK_YOURSELF);
        }

        BlockStatus blockStatus = getBlockStatus(currentUsername, username);
        switch (blockStatus) {
            case BLOCKED -> throw new ApiException(ErrorCode.HAS_BLOCKED);
            case HAS_BEEN_BLOCKED -> throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
            case NORMAL -> {
                if (currentUser.getBlockCount() + 1 > User.MAX_BLOCK_COUNT)
                    throw new ApiException(ErrorCode.BLOCK_LIMIT_REACHED);

                blockRepository.blockUser(currentUsername, username);
                relationshipCacheRepository.invalidateBlock(currentUsername);

                MessageCommand command = MessageCommand.builder()
                        .command(MessageCommand.Command.HAS_BEEN_BLOCKED)
                        .id(currentUser.getId().toString())
                        .build();
                eventPublisher.publishEvent(new CommandEvent(command, inChatRepository.getChatId(currentUser.getId(), targetUser.getId())));
            }
        }
    }

    @Override
    public void unblock(String username) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        User target = userService.getUser(username);
        UUID uuid = blockRepository.getBlockId(currentUserId, target.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BLOCK_NOT_FOUND));
        blockRepository.deleteByUuid(uuid);
        relationshipCacheRepository.invalidateBlock(userService.getCurrentUsernameRequiredAuthentication());
        MessageCommand command = MessageCommand.builder()
                .command(MessageCommand.Command.HAS_BEEN_UNBLOCKED)
                .id(String.valueOf(currentUserId))
                .build();
        eventPublisher.publishEvent(new CommandEvent(command, inChatRepository.getChatId(currentUserId, target.getId())));
    }

    @Override
    public List<UserCommonInformationResponse> getBlockedUsers() {
        String username = userService.getCurrentUsernameRequiredAuthentication();
        return relationshipCacheRepository.getBlock(username);
    }
}
