package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.event.CommandEvent;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.BlockRepository;
import com.stu.socialnetworkapi.service.itf.BlockService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.UserCounterCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final UserCounterCalculator userCounterCalculator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void validateBlock(UUID userId, UUID targetId) {
        BlockStatus blockStatus = getBlockStatus(userId, targetId);
        if (Objects.requireNonNull(blockStatus) == BlockStatus.BLOCKED) {
            throw new ApiException(ErrorCode.HAS_BLOCKED);
        } else if (blockStatus == BlockStatus.HAS_BEEN_BLOCKED) {
            throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
        }
    }

    @Override
    public BlockStatus getBlockStatus(UUID userId, UUID targetId) {
        return blockRepository.getBlockStatus(userId, targetId);
    }

    @Override
    public void block(String username) {
        User user = userService.getCurrentUserRequiredAuthentication();
        User target = userService.getUser(username);

        if (user.getId().equals(target.getId())) {
            throw new ApiException(ErrorCode.CAN_NOT_BLOCK_YOURSELF);
        }

        BlockStatus blockStatus = getBlockStatus(user.getId(), target.getId());
        switch (blockStatus) {
            case BLOCKED -> throw new ApiException(ErrorCode.HAS_BLOCKED);
            case HAS_BEEN_BLOCKED -> throw new ApiException(ErrorCode.HAS_BEEN_BLOCKED);
            case NORMAL -> {
                if (user.getBlockCount() + 1 > User.MAX_BLOCK_COUNT)
                    throw new ApiException(ErrorCode.BLOCK_LIMIT_REACHED);
                blockRepository.blockUser(user.getId(), target.getId());
                userCounterCalculator.calculateUserCounter(user.getId());
                MessageCommand command = MessageCommand.builder()
                        .command(MessageCommand.Command.HAS_BEEN_BLOCKED)
                        .id(user.getId())
                        .build();
                eventPublisher.publishEvent(new CommandEvent(this, command, target.getId()));
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
        userCounterCalculator.calculateUserCounter(currentUserId);
        MessageCommand command = MessageCommand.builder()
                .command(MessageCommand.Command.HAS_BEEN_UNBLOCKED)
                .id(currentUserId)
                .build();
        eventPublisher.publishEvent(new CommandEvent(this, command, target.getId()));
    }

    @Override
    public List<UserCommonInformationResponse> getBlockedUsers(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return blockRepository.getBlockedUsers(currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }
}
