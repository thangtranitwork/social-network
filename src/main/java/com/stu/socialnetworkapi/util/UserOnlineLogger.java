package com.stu.socialnetworkapi.util;

import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import com.stu.socialnetworkapi.repository.redis.IsOnlineRepository;
import com.stu.socialnetworkapi.repository.sqlite.OnlineUserLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserOnlineLogger {
    private final IsOnlineRepository isOnlineRepository;
    private final OnlineUserLogRepository onlineUserLogRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional("sqliteTransactionManager")
    public void logUserOnline() {
        int count = isOnlineRepository.countOnlineUsers();
        LocalDateTime now = LocalDateTime.now();
        onlineUserLogRepository.save(OnlineUserLog.builder()
                .timestamp(now)
                .onlineCount(count)
                .build());
        log.info("User online count: {} at timestamp {}", count, Timestamp.valueOf(now));
    }
}
