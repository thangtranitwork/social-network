package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.NotificationResponse;
import com.stu.socialnetworkapi.entity.Notification;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NotificationService {
    NotificationResponse save(Notification notification);

    void send(Notification notification);

    List<NotificationResponse> getNotifications(Pageable pageable);
}
