package com.stu.socialnetworkapi.event;

import com.nimbusds.jose.shaded.gson.Gson;
import com.stu.socialnetworkapi.repository.neo4j.KeywordRepository;
import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordExtractEventConsumer {
    private final Gson gson;
    private final KeywordRepository keywordRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KeywordExtractorService keywordExtractorService;
    private ScheduledExecutorService scheduledExecutorService;

    private static final String KEYWORD_EXTRACT_EVENT_QUEUE = "queue:keyword_extract_event";

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.submit(() -> {
            while (true) {
                String result = redisTemplate.opsForList()
                        .leftPop(KEYWORD_EXTRACT_EVENT_QUEUE, Duration.ofSeconds(5));
                if (result != null) {
                    KeywordExtractEvent event = gson.fromJson(result, KeywordExtractEvent.class);
                    handle(event);
                }
            }
        });
    }

    @Retryable(
            backoff = @Backoff(delay = 2000, multiplier = 1)
    )
    private void handle(KeywordExtractEvent event) {
        List<String> keywords = keywordExtractorService.extract(event.getContent());
        if (keywords.isEmpty()) {
            return;
        }
        if (!event.isUpdate()) {
            keywordRepository.save(event.getPostId(), keywords);
        } else {
            keywordRepository.update(event.getPostId(), keywords);
        }
    }

    @PreDestroy
    public void destroy() {
        // Shutdown thread để tránh memory leaks
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
    }
}
