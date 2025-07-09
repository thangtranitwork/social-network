package com.stu.socialnetworkapi.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IsTypingRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String IS_TYPING_KEY = "is_typing:";
    private static final String IS_TYPING_CHAT_KEY = "is_typing_chat:";

    public void save(UUID userId, UUID chatId) {
        redisTemplate.opsForValue().set(IS_TYPING_KEY + userId, chatId.toString());
        redisTemplate.opsForSet().add(IS_TYPING_CHAT_KEY + chatId, userId.toString());
    }

    public UUID getChatId(UUID userId) {
        String chatIdStr = redisTemplate.opsForValue().get(IS_TYPING_KEY + userId);
        return chatIdStr != null ? UUID.fromString(chatIdStr) : null;
    }

    public Set<String> getTypingUsersInChat(UUID chatId) {
        return redisTemplate.opsForSet().members(IS_TYPING_KEY + chatId);
    }

    public void delete(UUID userId, UUID chatId) {
        redisTemplate.delete(IS_TYPING_KEY + userId);
        redisTemplate.opsForSet().remove(IS_TYPING_CHAT_KEY + chatId, userId);
    }

    @PostConstruct
    public void init() {
        try {
            redisTemplate.keys(IS_TYPING_KEY + "*")
                    .forEach(redisTemplate::delete);

            redisTemplate.keys(IS_TYPING_CHAT_KEY + "*")
                    .forEach(redisTemplate::delete);

        } catch (Exception e) {
            log.error("Error clearing Redis typing user state: {}", e.getMessage(), e);
        }
    }
}
