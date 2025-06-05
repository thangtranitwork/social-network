package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.NotificationProjection;
import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final UserMapper userMapper;

    public NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .creator(userMapper.toUserCommonInformationResponse(notification.getCreator()))
                .action(notification.getAction())
                .targetId(notification.getTargetId())
                .targetType(notification.getTargetType())
                .sentAt(notification.getSentAt())
                .build();
    }

    public NotificationResponse toNotificationResponse(NotificationProjection projection) {
        return NotificationResponse.builder()
                .id(projection.id())
                .action(projection.action())
                .targetId(projection.targetId())
                .targetType(projection.targetType())
                .sentAt(projection.sentAt())
                .creator(userMapper.toUserCommonInformationResponse(projection))
                .build();
    }
}
