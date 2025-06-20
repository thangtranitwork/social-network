package com.stu.socialnetworkapi.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
public class TargetChatIdRedisRepository {

    private final ChatRepository chatRepository;
    private final RedisTemplate<String, List<String>> redisTemplate;

    // Lombok @RequiredArgsConstructor không xử lý @Qualifier
    public TargetChatIdRedisRepository(
            @Qualifier("uuidListRedisTemplate")
            RedisTemplate<String, List<String>> redisTemplate,
            ChatRepository chatRepository) {
        this.redisTemplate = redisTemplate;
        this.chatRepository = chatRepository;
    }

    private static final Duration TTL = Duration.ofHours(2);
    private static final String CHAT_TARGET_IDS_KEY = "chat:targets:";

    public List<UUID> getTargetChatIds(UUID userId) {
        String redisKey = CHAT_TARGET_IDS_KEY + userId;
        ValueOperations<String, List<String>> ops = redisTemplate.opsForValue();

        // 1. Check Redis
        List<String> targetIds = ops.get(redisKey);
        if (targetIds != null) {
            return targetIds.stream().map(UUID::fromString)
                    .toList();
        }

        // 2. Nếu chưa có → truy vấn DB
        List<UUID> uuidList = chatRepository.getTargetIds(userId);
        targetIds = uuidList.stream().map(UUID::toString).toList();
        // 3. Lưu vào Redis với TTL 2 giờ
        ops.set(redisKey, targetIds, TTL);

        return uuidList;
    }

    public void invalidate(UUID userId) {
        String redisKey = CHAT_TARGET_IDS_KEY + userId;
        redisTemplate.delete(redisKey);
    }
}
