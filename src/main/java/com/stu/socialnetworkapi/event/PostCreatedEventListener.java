package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostCreatedEventListener {
    private final KeywordExtractorService keywordExtractorService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        keywordExtractorService.extract(event.getPostId());
    }
}
