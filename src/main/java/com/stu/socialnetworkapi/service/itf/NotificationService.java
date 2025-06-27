package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.dto.response.NotificationsResponse;
import com.stu.socialnetworkapi.entity.Notification;

public interface NotificationService {
    NotificationResponse save(Notification notification);

    void send(Notification notification);

    void sendToFriends(Notification notification);

    NotificationsResponse getNotifications(Neo4jPageable pageable);

    long getUnreadNotificationCount();

    void markLatestNotificationsAsRead(long limit);
}
