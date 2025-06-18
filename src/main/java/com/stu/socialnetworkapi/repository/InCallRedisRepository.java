package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InCallRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String INCALL_KEY = "incall:";

    public void call(UUID caller, UUID callee) {
        redisTemplate.opsForValue().set(INCALL_KEY + caller, callee.toString());
        redisTemplate.opsForValue().set(INCALL_KEY + callee, caller.toString());
    }

    public void endCall(UUID caller, UUID callee) {
        redisTemplate.delete(INCALL_KEY + caller);
        redisTemplate.delete(INCALL_KEY + callee);
    }

    public boolean isInCall(UUID userId) {
        return redisTemplate.hasKey(INCALL_KEY + userId);
    }
}
