package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.response.OnlineResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class TargetChatIdRedisRepository {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, List<UUID>> redisTemplate;

    // Lombok @RequiredArgsConstructor không xử lý @Qualifier
    public TargetChatIdRedisRepository(
            @Qualifier("uuidListRedisTemplate")
            RedisTemplate<String, List<UUID>> redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            ChatRepository chatRepository) {
        this.redisTemplate = redisTemplate;
        this.chatRepository = chatRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private static final Duration TTL = Duration.ofHours(2);
    private static final String CHAT_TARGET_IDS_KEY = "chat:targets:";

    public List<UUID> getTargetChatIds(UUID userId) {
        String redisKey = CHAT_TARGET_IDS_KEY + userId;
        ValueOperations<String, List<UUID>> ops = redisTemplate.opsForValue();

        // 1. Check Redis
        List<UUID> targetIds = ops.get(redisKey);
        if (targetIds != null) {
            return targetIds;
        }

        // 2. Nếu chưa có → truy vấn DB
        targetIds = chatRepository.getTargetIds(userId);
        if (targetIds == null) targetIds = Collections.emptyList();

        // 3. Lưu vào Redis với TTL 2 giờ
        ops.set(redisKey, targetIds, TTL);

        return targetIds;
    }

    public void invalidate(UUID userId) {
        String redisKey = CHAT_TARGET_IDS_KEY + userId;
        redisTemplate.delete(redisKey);
    }


    // Async method can not be private
    @Async
    public void sendToChatTarget(UUID userId, boolean isOnline, ZonedDateTime lastOnline) {
        OnlineResponse response = OnlineResponse.builder()
                .userId(userId)
                .isOnline(isOnline)
                .lastOnline(lastOnline)
                .build();
        getTargetChatIds(userId)
                .forEach(id ->
                        messagingTemplate.convertAndSend(WebSocketChannelPrefix.ONLINE_CHANNEL_PREFIX + "/" + id, response));
    }
}
