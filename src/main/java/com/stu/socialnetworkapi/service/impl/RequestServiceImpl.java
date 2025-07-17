package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.RequestRepository;
import com.stu.socialnetworkapi.repository.redis.RelationshipCacheRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import com.stu.socialnetworkapi.service.itf.RequestService;
import com.stu.socialnetworkapi.service.itf.UserService;
import com.stu.socialnetworkapi.util.UserCounterCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final ChatService chatService;
    private final RequestRepository requestRepository;
    private final NotificationService notificationService;
    private final UserCounterCalculator userCounterCalculator;
    private final RelationshipCacheRepository relationshipCacheRepository;

    @Override
    public void sendAddFriendRequest(String username) {
        User requester = userService.getCurrentUserRequiredAuthentication();
        User target = userService.getUser(username);

        validateSendAddFriendRequest(requester, target);
        requestRepository.create(requester.getId(), target.getId());

        userCounterCalculator.calculateUsersCounter(List.of(requester.getId(), target.getId()));
        relationshipCacheRepository.invalidateRequestSent(requester.getUsername());
        relationshipCacheRepository.invalidateRequestReceived(target.getUsername());
        relationshipCacheRepository.removeIfInSuggestion(requester.getUsername(), target.getUsername());
        relationshipCacheRepository.removeIfInSuggestion(target.getUsername(), requester.getUsername());

        Notification notification = Notification.builder()
                .creator(requester)
                .receiver(target)
                .action(NotificationAction.SENT_ADD_FRIEND_REQUEST)
                .targetId(target.getId())
                .targetType(ObjectType.REQUEST)
                .build();
        notificationService.send(notification);
    }

    @Override
    public List<UserCommonInformationResponse> getSentRequests() {
        String username = userService.getCurrentUsernameRequiredAuthentication();
        return relationshipCacheRepository.getRequestSent(username);
    }

    @Override
    public List<UserCommonInformationResponse> getReceivedRequests() {
        String username = userService.getCurrentUsernameRequiredAuthentication();
        return relationshipCacheRepository.getRequestReceived(username);
    }

    @Override
    public void deleteRequest(String username) {
        String currentUsername = userService.getCurrentUsernameRequiredAuthentication();
        userService.validateUserExists(username);
        UUID uuid = requestRepository.getRequestUUID(currentUsername, username)
                .orElseThrow(() -> new ApiException(ErrorCode.REQUEST_NOT_FOUND));

        requestRepository.deleteByUuid(uuid);
        userCounterCalculator.calculateUsersCounterByUsername(List.of(currentUsername, username));

        relationshipCacheRepository.getRequestSent(username);
        relationshipCacheRepository.getRequestReceived(username);
        relationshipCacheRepository.invalidateRequestSent(currentUsername);
        relationshipCacheRepository.invalidateRequestReceived(currentUsername);
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
        relationshipCacheRepository.invalidateRequestSent(currentUser.getUsername());
        relationshipCacheRepository.invalidateRequestReceived(target.getUsername());
        relationshipCacheRepository.invalidateFriend(currentUser.getUsername());
        relationshipCacheRepository.invalidateFriend(target.getUsername());
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
