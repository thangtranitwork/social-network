package com.stu.socialnetworkapi.event;

import com.stu.socialnetworkapi.repository.redis.RelationshipCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateUsernameEventListener {
    private final RelationshipCacheRepository relationshipCacheRepository;

    @Async
    @EventListener
    public void handleUpdateUsernameEvent(UpdateUsernameEvent event) {
        relationshipCacheRepository.updateUsername(event.getOldUsername(), event.getNewUsername());
    }
}
