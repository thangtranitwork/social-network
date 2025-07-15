package com.stu.socialnetworkapi.util;

import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCounterCalculator {
    private final UserRepository userRepository;

    @Async
    public void calculateUserCounter(UUID userId) {
        userRepository.recalculateUserCounters(userId);
    }

    @Async
    public void calculateUsersCounter(List<UUID> userIds) {
        userIds.forEach(userRepository::recalculateUserCounters);
    }
}
