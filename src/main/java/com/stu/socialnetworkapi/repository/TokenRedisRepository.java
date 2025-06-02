package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";
    private static final String REFRESH_TOKEN_REVERSE_KEY = "refresh_token_reverse:";
    private static final String USER_ROLE_KEY = "user_role:";
    private static final String USER_USERNAME_KEY = "user_username:";

    public void save(UUID userId, String token, String role, String username, Duration ttl) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY + userId, token, ttl);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_REVERSE_KEY + token, userId.toString(), ttl);
        redisTemplate.opsForValue().set(USER_ROLE_KEY + userId, role, ttl);
        redisTemplate.opsForValue().set(USER_USERNAME_KEY + userId, username, ttl);
    }

    public Optional<String> getRefreshToken(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + userId));
    }

    public Optional<String> findUserIdByToken(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_TOKEN_REVERSE_KEY + token));
    }

    public Optional<String> getRole(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(USER_ROLE_KEY + userId));
    }

    public Optional<String> getUsername(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(USER_USERNAME_KEY + userId));
    }

    public void delete(String token) {
        findUserIdByToken(token).ifPresent(userId -> {
            redisTemplate.delete(REFRESH_TOKEN_KEY + userId);
            redisTemplate.delete(USER_ROLE_KEY + userId);
            redisTemplate.delete(REFRESH_TOKEN_REVERSE_KEY + token);
            redisTemplate.delete(USER_USERNAME_KEY + userId);
        });
    }
}

