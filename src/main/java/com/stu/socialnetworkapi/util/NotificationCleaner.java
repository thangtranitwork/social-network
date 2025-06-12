package com.stu.socialnetworkapi.util;

import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCleaner {
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void clean() {
        ZonedDateTime cutOff = ZonedDateTime.now().minusDays(Notification.DAY_ALIVE);
        notificationRepository.deleteOldNotifications(cutOff);
        log.debug("Deleted notifications before {}", cutOff);
    }
}
