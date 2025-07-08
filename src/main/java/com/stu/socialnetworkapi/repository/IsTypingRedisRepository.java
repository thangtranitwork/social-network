package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IsTypingRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String IS_TYPING_KEY = "is_typing:";

    public void save(UUID userId, UUID chatId) {
        redisTemplate.opsForValue().set(IS_TYPING_KEY + userId, chatId.toString());
    }

    public UUID getChatId(UUID userId) {
        String chatIdStr = redisTemplate.opsForValue().get(IS_TYPING_KEY + userId);
        return chatIdStr != null ? UUID.fromString(chatIdStr) : null;
    }


    public void delete(UUID userId) {
        redisTemplate.delete(IS_TYPING_KEY + userId);
    }
}
