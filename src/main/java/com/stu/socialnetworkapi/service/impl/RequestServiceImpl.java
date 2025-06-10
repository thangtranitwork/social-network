package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.RequestResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.mapper.RequestMapper;
import com.stu.socialnetworkapi.repository.RequestRepository;
import com.stu.socialnetworkapi.service.itf.ChatService;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import com.stu.socialnetworkapi.service.itf.RequestService;
import com.stu.socialnetworkapi.service.itf.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final ChatService chatService;
    private final RequestMapper requestMapper;
    private final RequestRepository requestRepository;
    private final NotificationService notificationService;

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

        notificationService.send(notification);
    }

    @Override
    public List<RequestResponse> getSentRequests(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return requestRepository.getSentRequest(currentUserId, pageable).stream()
                .map(requestMapper::toRequestResponse)
                .toList();
    }

    @Override
    public List<RequestResponse> getReceivedRequests(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return requestRepository.getReceivedRequest(currentUserId, pageable).stream()
                .map(requestMapper::toRequestResponse)
                .toList();
    }

    @Override
    public void deleteRequest(UUID requestId) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        if (!requestRepository.canDeleteRequest(requestId, currentUserId)) {
            throw new ApiException(ErrorCode.REQUEST_NOT_FOUND);
        }
        requestRepository.delete(requestId);
    }

    @Override
    public void acceptRequest(UUID requestId) {
        if (!requestRepository.existsByUuid(requestId)) {
            throw new ApiException(ErrorCode.REQUEST_NOT_FOUND);
        }

        UUID targetId = requestRepository.getTargetId(requestId);
        User target = userService.getUser(targetId);
        User user = userService.getCurrentUserRequiredAuthentication();

        if (!target.getId().equals(user.getId())) {
            throw new ApiException(ErrorCode.ACCEPT_REQUEST_FAILED);
        }
        UUID senderId = requestRepository.getSenderId(requestId);
        User sender = userService.getUser(senderId);
        requestRepository.acceptRequest(requestId);
        chatService.createChatIfNotExist(sender, target);
        Notification notification = Notification.builder()
                .action(NotificationAction.BE_FRIEND)
                .targetId(target.getId())
                .targetType(ObjectType.USER)
                .creator(user)
                .receiver(sender)
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
