package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.RequestRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import com.stu.socialnetworkapi.service.itf.RequestService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.UserCounterCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final ChatService chatService;
    private final RequestRepository requestRepository;
    private final NotificationService notificationService;
    private final UserCounterCalculator userCounterCalculator;

    @Override
    public void sendAddFriendRequest(String username) {
        User requester = userService.getCurrentUserRequiredAuthentication();
        User target = userService.getUser(username);

        validateSendAddFriendRequest(requester, target);
        requestRepository.create(requester.getId(), target.getId());

        Notification notification = Notification.builder()
                .creator(requester)
                .receiver(target)
                .action(NotificationAction.SENT_ADD_FRIEND_REQUEST)
                .targetId(target.getId())
                .targetType(ObjectType.REQUEST)
                .build();
        userCounterCalculator.calculateUsersCounter(List.of(requester.getId(), target.getId()));
        notificationService.send(notification);
    }

    @Override
    public List<UserCommonInformationResponse> getSentRequests(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return requestRepository.getSentRequest(currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }

    @Override
    public List<UserCommonInformationResponse> getReceivedRequests(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return requestRepository.getReceivedRequest(currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }

    @Override
    public void deleteRequest(String username) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        User target = userService.getUser(username);
        UUID uuid = requestRepository.getRequestUUID(target.getId(), currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.REQUEST_NOT_FOUND));

        requestRepository.deleteByUuid(uuid);
        userCounterCalculator.calculateUsersCounter(List.of(currentUserId, target.getId()));
    }

    @Override
    public void acceptRequest(String username) {
        User currentUser = userService.getCurrentUserRequiredAuthentication();
        User target = userService.getUser(username);
        UUID uuid = requestRepository.getRequestUUIDWhichDirection(target.getId(), currentUser.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.REQUEST_NOT_FOUND));

        requestRepository.acceptRequest(uuid);
        userCounterCalculator.calculateUsersCounter(List.of(currentUser.getId(), target.getId()));

        chatService.createChatIfNotExist(currentUser, target);
        Notification notification = Notification.builder()
                .action(NotificationAction.BE_FRIEND)
                .targetId(target.getId())
                .targetType(ObjectType.USER)
                .creator(currentUser)
                .receiver(target)
                .build();
        notificationService.send(notification);
    }

    private void validateSendAddFriendRequest(User requester, User target) {
        if (requester.getId().equals(target.getId())) {
            throw new ApiException(ErrorCode.CAN_NOT_MAKE_SELF_REQUEST);
        }
        if (!requestRepository.canSendRequest(requester.getId(), target.getId())) {
            throw new ApiException(ErrorCode.SENT_ADD_FRIEND_REQUEST_FAILED);
        }
        if (requester.getRequestSentCount() + 1 > User.MAX_SENT_REQUEST_COUNT) {
            throw new ApiException(ErrorCode.ADD_FRIEND_REQUEST_SENT_LIMIT_REACHED);
        }
        if (target.getRequestReceivedCount() + 1 > User.MAX_RECEIVED_REQUEST_COUNT) {
            throw new ApiException(ErrorCode.ADD_FRIEND_REQUEST_RECEIVED_LIMIT_REACHED);
        }
    }
}
