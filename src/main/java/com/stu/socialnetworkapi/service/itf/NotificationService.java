package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;

import java.util.List;

public interface NotificationService {
    NotificationResponse save(Notification notification);

    void send(Notification notification);

    void sendToFriends(Notification notification);

    List<NotificationResponse> getNotifications(Neo4jPageable pageable);
}
