package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LastSeenRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String LAST_SEEN_KEY = "user_last_seen:";

    public void save(UUID userId, ZonedDateTime lastSeen) {
        redisTemplate.opsForValue().set(LAST_SEEN_KEY + userId, lastSeen.toString());
    }

    public ZonedDateTime getLastSeen(UUID userId) {
        String lastSeenStr = redisTemplate.opsForValue().get(LAST_SEEN_KEY + userId);
        if (lastSeenStr == null) return null;
        return ZonedDateTime.parse(lastSeenStr);
    }
}
