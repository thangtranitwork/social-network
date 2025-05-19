package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.mapper.NotificationMapper;
import com.stu.socialnetworkapi.repository.NotificationRepository;
import com.stu.socialnetworkapi.service.itf.NotificationService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserService userService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse save(Notification notification) {
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    public void send(Notification notification, User target) {
        String destination = "/api/notifications/" + target.getId();
        messagingTemplate.convertAndSend(destination, save(notification));
    }

    @Override
    public Slice<NotificationResponse> getNotifications(Pageable pageable) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        return notificationRepository.getNotifications(currentUserId, pageable)
                .map(notificationMapper::toNotificationResponse);

    }
}
