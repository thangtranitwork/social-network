package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IsOnlineRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String IS_ONLINE_KEY = "is_online:";
    private static final String LAST_ONLINE_KEY = "last_online:";

    public void save(UUID userId, boolean isOnline) {
        redisTemplate.opsForValue().set(IS_ONLINE_KEY + userId, isOnline ? "true" : "false");
        redisTemplate.opsForValue().set(LAST_ONLINE_KEY + userId, ZonedDateTime.now().toString());
    }

    public OnlineResponse getLastSeen(UUID userId) {
        boolean isOnline = Optional.ofNullable(redisTemplate.opsForValue().get(IS_ONLINE_KEY + userId))
                .map(Boolean::valueOf)
                .orElse(false);
        ZonedDateTime lastOnline = Optional.ofNullable(redisTemplate.opsForValue().get(LAST_ONLINE_KEY + userId))
                .map(ZonedDateTime::parse)
                .orElse(null);
        return OnlineResponse.builder()
                .isOnline(isOnline)
                .lastOnlineAt(lastOnline)
                .build();
    }


}
