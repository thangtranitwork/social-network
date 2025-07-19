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
public class PostCreatedEventConsumer {
    private final Gson gson;
    private final KeywordRepository keywordRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KeywordExtractorService keywordExtractorService;
    private ScheduledExecutorService scheduledExecutorService;

    private static final String POST_CREATED_EVENT_QUEUE = "queue:post_created";

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.submit(() -> {
            while (true) {
                String result = redisTemplate.opsForList()
                        .leftPop(POST_CREATED_EVENT_QUEUE, Duration.ofSeconds(5));
                if (result != null) {
                    PostCreatedEvent event = gson.fromJson(result, PostCreatedEvent.class);
                    handle(event);
                }
            }
        });
    }

    @Retryable(
            backoff = @Backoff(delay = 2000, multiplier = 1)
    )
    private void handle(PostCreatedEvent event) {
        List<String> keywords = keywordExtractorService.extract(event.getContent());

        try {
            keywordRepository.save(event.getPostId(), keywords);
        } catch (Exception e) {
            log.error(e.getMessage());
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
