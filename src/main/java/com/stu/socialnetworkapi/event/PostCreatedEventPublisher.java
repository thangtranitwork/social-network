package com.stu.socialnetworkapi.event;

import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostCreatedEventPublisher {
    private final Gson gson;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String POST_CREATED_EVENT_QUEUE = "queue:post_created";

    public void publish(PostCreatedEvent event) {
        String json = gson.toJson(event);
        redisTemplate.opsForList().rightPush(POST_CREATED_EVENT_QUEUE, json);
    }
}
