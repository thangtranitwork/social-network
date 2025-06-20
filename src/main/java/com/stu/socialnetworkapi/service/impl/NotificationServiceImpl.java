package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.mapper.NotificationMapper;
import com.stu.socialnetworkapi.repository.NotificationRepository;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserService userService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    private final Set<NotificationAction> actionCanBeRepeated = new HashSet<>(List.of(
            NotificationAction.LIKE_POST,
            NotificationAction.LIKE_COMMENT
    ));

    @Override
    public NotificationResponse save(Notification notification) {
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    @Async
    @Override
    public void sendToFriends(Notification notification) {
        List<User> friends = notification.getCreator().getFriends();
        List<Notification> notifications = friends.stream()
                .map(friend -> {
                    Notification noti = new Notification(notification);
                    noti.setReceiver(friend);
                    return noti;
                })
                .toList();

        notificationRepository.saveAll(notifications);

        notifications.forEach(noti ->
                messagingTemplate.convertAndSend(WebSocketChannelPrefix.NOTIFICATION_CHANNEL_PREFIX + "/" + noti.getReceiver().getId(), notificationMapper.toNotificationResponse(noti)));
    }

    @Override
    public void send(Notification notification) {
        if (notification.getCreator().equals(notification.getReceiver())) return;
        if (actionCanBeRepeated.contains(notification.getAction())) {
            Optional<UUID> optional = notificationRepository
                    .findExistingNotification(
                            notification.getCreator().getId(),
                            notification.getReceiver().getId(),
                            notification.getAction(),
                            notification.getTargetId(),
                            notification.getTargetType());
            if (optional.isPresent()) {
                notification.setId(optional.get());
                notification.setSentAt(ZonedDateTime.now());
            }
        }
        String destination = WebSocketChannelPrefix.NOTIFICATION_CHANNEL_PREFIX + "/" + notification.getReceiver().getId();
        messagingTemplate.convertAndSend(destination, save(notification));
    }

    @Override
    public List<NotificationResponse> getNotifications(Neo4jPageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return notificationRepository.getNotifications(currentUserId, pageable.getSkip(), pageable.getLimit()).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();

    }
}
