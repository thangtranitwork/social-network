package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NotificationService {
    NotificationResponse save(Notification notification);

    void send(Notification notification, User user);

    Slice<NotificationResponse> getNotifications(Pageable pageable);
}
