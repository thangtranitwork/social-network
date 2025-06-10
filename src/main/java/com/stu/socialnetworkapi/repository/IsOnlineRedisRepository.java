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
    private static final String ONLINE_COUNT_KEY = "online_user_count";

    public void save(UUID userId, boolean isOnline) {
        String id = userId.toString();

        boolean wasOnline = Optional.ofNullable(redisTemplate.opsForValue().get(IS_ONLINE_KEY + id))
                .map(Boolean::valueOf)
                .orElse(false);

        redisTemplate.opsForValue().set(IS_ONLINE_KEY + id, String.valueOf(isOnline));
        redisTemplate.opsForValue().set(LAST_ONLINE_KEY + id, ZonedDateTime.now().toString());

        // Chỉ thay đổi count khi trạng thái thực sự thay đổi
        if (isOnline && !wasOnline) {
            redisTemplate.opsForValue().increment(ONLINE_COUNT_KEY);
        } else if (!isOnline && wasOnline) {
            redisTemplate.opsForValue().decrement(ONLINE_COUNT_KEY);
        }

        System.out.println("User " + id + " is " + (isOnline ? "online" : "offline"));
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

    public int countOnlineUsers() {
        String countStr = redisTemplate.opsForValue().get(ONLINE_COUNT_KEY);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }
}
