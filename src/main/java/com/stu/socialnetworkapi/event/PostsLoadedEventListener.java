package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.repository.neo4j.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostsLoadedEventListener {
    private final PostRepository postRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPostsLoaded(final PostsLoadedEvent event) {
        postRepository.increaseLoaded(event.postIds, event.userId);
    }
}
