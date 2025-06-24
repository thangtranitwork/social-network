package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InCallRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String INCALL_KEY = "incall:";

    public void call(String caller, String callee) {
        redisTemplate.opsForValue().set(INCALL_KEY + caller, callee);
        redisTemplate.opsForValue().set(INCALL_KEY + callee, caller);
    }

    public void endCall(String caller, String callee) {
        redisTemplate.delete(INCALL_KEY + caller);
        redisTemplate.delete(INCALL_KEY + callee);
    }

    public boolean isInCall(String username) {
        return redisTemplate.hasKey(INCALL_KEY + username);
    }
}
