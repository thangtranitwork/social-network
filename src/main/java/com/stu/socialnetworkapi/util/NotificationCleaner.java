package com.stu.socialnetworkapi.util;

import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class NotificationCleaner {
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void clean() {
        ZonedDateTime cutOff = ZonedDateTime.now().minusDays(Notification.DAY_ALIVE);
        int deleted = notificationRepository.deleteOldNotifications(cutOff);
        System.out.println("Deleted " + deleted + " notifications");
    }
}
