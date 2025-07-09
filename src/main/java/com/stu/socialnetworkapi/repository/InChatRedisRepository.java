package com.stu.socialnetworkapi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InChatRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRepository chatRepository;
    private static final String USER_CHATS_KEY = "user-chat:";
    private static final String SUBSCRIBED_CHAT_KEY = "subscribed-chat:";

    public void save(UUID userId, UUID chatId) {
        String key = USER_CHATS_KEY + userId;
        redisTemplate.opsForSet().add(key, chatId.toString());
    }

    public void save(UUID userId, List<UUID> chatIds) {
        String key = USER_CHATS_KEY + userId;
        String[] chatIdsArray = chatIds.stream().map(UUID::toString).toArray(String[]::new);
        redisTemplate.opsForSet().add(key, chatIdsArray);
    }

    public void subscribe(UUID userId, UUID chatId) {
        String key = SUBSCRIBED_CHAT_KEY + userId;
        redisTemplate.opsForSet().add(key, chatId.toString());
    }

    public void unsubscribeAll(UUID userId) {
        String key = SUBSCRIBED_CHAT_KEY + userId;
        redisTemplate.delete(key);
    }

    public void unsubscribe(UUID userId, UUID chatId) {
        String key = SUBSCRIBED_CHAT_KEY + userId;
        redisTemplate.opsForSet().remove(key, chatId.toString());
    }

    public boolean isSubscribed(UUID userId, UUID chatId) {
        String key = SUBSCRIBED_CHAT_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, chatId.toString()));
    }

    public boolean isInChat(UUID userId, UUID chatId) {
        String key = USER_CHATS_KEY + userId;
        if (!redisTemplate.hasKey(key)) {
            save(userId, chatRepository.getChatIdsByUserId(userId));
        }
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, chatId.toString()));
    }
}
