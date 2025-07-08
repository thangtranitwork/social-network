package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.request.UserTypingRequest;
import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import com.stu.socialnetworkapi.event.TypingEvent;
import com.stu.socialnetworkapi.event.UserOnlineEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IsOnlineRedisRepository {
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final IsTypingRedisRepository isTypingRedisRepository;
    private static final String ONLINE_COUNT_KEY = "online_user_count";
    private static final String USER_ONLINE_COUNTER_KEY = "user_online_counter:";
    private static final String LAST_ONLINE_KEY = "last_online:";

    public void onUserConnected(UUID userId) {
        String userKey = userId.toString();
        Long count = redisTemplate.opsForValue().increment(USER_ONLINE_COUNTER_KEY + userKey);

        // Nếu lần đầu tiên online → tăng tổng số user online
        if (count != null && count == 1L) {
            redisTemplate.opsForValue().increment(ONLINE_COUNT_KEY);
            log.debug("User {} is now ONLINE", userKey);
            eventPublisher.publishEvent(new UserOnlineEvent(this, userId, true, null));
        }
    }

    public void onUserDisconnected(UUID userId) {
        String userKey = userId.toString();
        Long count = redisTemplate.opsForValue().decrement(USER_ONLINE_COUNTER_KEY + userKey);

        if (count != null && count < 0) {
            redisTemplate.delete(USER_ONLINE_COUNTER_KEY + userKey);
            UUID chatId = isTypingRedisRepository.getChatId(userId);
            if (chatId != null) {
                UserTypingRequest request = new UserTypingRequest(chatId, userId, false);
                eventPublisher.publishEvent(new TypingEvent(this, request));
            }
            return;
        }
        if (count == null || count == 0) {
            // User thực sự offline
            redisTemplate.delete(USER_ONLINE_COUNTER_KEY + userKey);
            redisTemplate.opsForValue().decrement(ONLINE_COUNT_KEY);
            ZonedDateTime now = ZonedDateTime.now();
            redisTemplate.opsForValue().set(LAST_ONLINE_KEY + userKey, now.toString());
            log.debug("User {} is now OFFLINE", userKey);
            eventPublisher.publishEvent(new UserOnlineEvent(this, userId, false, now));
        }
    }

    public OnlineResponse getLastSeen(UUID userId) {
        String userKey = userId.toString();

        long sessionCount = Optional.ofNullable(redisTemplate.opsForValue().get(USER_ONLINE_COUNTER_KEY + userKey))
                .map(Long::parseLong)
                .orElse(0L);
        boolean isOnline = sessionCount > 0;

        ZonedDateTime lastOnline = Optional.ofNullable(redisTemplate.opsForValue().get(LAST_ONLINE_KEY + userKey))
                .map(ZonedDateTime::parse)
                .orElse(null);

        return OnlineResponse.builder()
                .isOnline(isOnline)
                .lastOnline(lastOnline)
                .build();
    }

    public int countOnlineUsers() {
        String countStr = redisTemplate.opsForValue().get(ONLINE_COUNT_KEY);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    @PostConstruct
    public void init() {
        try {
            // Xoá toàn bộ user_online_counter:* key
            redisTemplate.keys(USER_ONLINE_COUNTER_KEY + "*")
                    .forEach(redisTemplate::delete);

            redisTemplate.keys(LAST_ONLINE_KEY + "*")
                    .forEach(redisTemplate::delete);

            redisTemplate.delete(ONLINE_COUNT_KEY);
        } catch (Exception e) {
            log.error("Error clearing Redis online user state: {}", e.getMessage(), e);
        }
    }
}
