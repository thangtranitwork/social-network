package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationsResponse {
    List<NotificationResponse> notifications;
    long unreadNotificationCount;
}
