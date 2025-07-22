package com.stu.socialnetworkapi.event;

import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordExtractEventPublisher {
    private final Gson gson;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEYWORD_EXTRACT_EVENT_QUEUE = "queue:keyword_extract_event";

    public void publish(KeywordExtractEvent event) {
        String json = gson.toJson(event);
        redisTemplate.opsForList().rightPush(KEYWORD_EXTRACT_EVENT_QUEUE, json);
    }
}
