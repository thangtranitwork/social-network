package com.stu.socialnetworkapi.repository.redis;

import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.dto.response.UserCommonInformationResponse;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.neo4j.BlockRepository;
import com.stu.socialnetworkapi.repository.neo4j.FriendRepository;
import com.stu.socialnetworkapi.repository.neo4j.RequestRepository;
import com.stu.socialnetworkapi.repository.neo4j.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RelationshipCacheRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final FriendRepository friendRepository;
    private final RequestRepository requestRepository;

    // key: + username
    private static final String FRIEND_KEY = "friends:";
    private static final String BLOCK_KEY = "blocks:";
    private static final String REQUEST_SENT_KEY = "requests_sent:";
    private static final String REQUEST_RECEIVED_KEY = "requests_received:";
    private static final String SUGGESTED_KEY = "suggested_friends:";
    private static final int SUGGESTED_VALIDITY_HOURS = 12;

    public List<UserCommonInformationResponse> getFriend(String username) {
        String key = FRIEND_KEY + username;
        List<UserProjection> projections;
        if (redisTemplate.hasKey(key)) projections = readOnCache(key);
        else {
            Set<String> usernames = friendRepository.getFriendUsernames(username, User.MAX_FRIEND_COUNT);
            projections = saveToRedis(key, usernames);
        }
        return mapToResponse(projections);
    }

    public List<UserCommonInformationResponse> getBlock(String username) {
        String key = BLOCK_KEY + username;
        List<UserProjection> projections;
        if (redisTemplate.hasKey(key)) projections = readOnCache(key);
        else {
            Set<String> usernames = blockRepository.getBlockedUserUsernames(username, User.MAX_BLOCK_COUNT);
            projections = saveToRedis(key, usernames);
        }
        return mapToResponse(projections);
    }

    public List<UserCommonInformationResponse> getSuggestedFriends(String username) {
        String key = SUGGESTED_KEY + username;
        List<UserProjection> projections;
        if (redisTemplate.hasKey(key)) projections = readOnCache(key);
        else {
            Set<String> usernames = friendRepository.getSuggestedFriendUsernames(username, User.MAX_SUGGESTED_FRIEND_COUNT);
            projections = saveToRedis(key, usernames);
        }
        return mapToResponse(projections);
    }

    public List<UserCommonInformationResponse> getRequestSent(String username) {
        String key = REQUEST_SENT_KEY + username;
        List<UserProjection> projections;
        if (redisTemplate.hasKey(key)) projections = readOnCache(key);
        else {
            Set<String> usernames = requestRepository.getSentRequestUsernames(username, User.MAX_SENT_REQUEST_COUNT);
            projections = saveToRedis(key, usernames);
        }
        return mapToResponse(projections);
    }

    public List<UserCommonInformationResponse> getRequestReceived(String username) {
        String key = REQUEST_RECEIVED_KEY + username;
        List<UserProjection> projections;
        if (redisTemplate.hasKey(key)) projections = readOnCache(key);
        else {
            Set<String> usernames = requestRepository.getReceivedRequestUsernames(username, User.MAX_SENT_REQUEST_COUNT);
            projections = saveToRedis(key, usernames);
        }
        return mapToResponse(projections);
    }

    public void invalidateFriend(String username) {
        redisTemplate.delete(FRIEND_KEY + username);
    }

    public void invalidateBlock(String username) {
        redisTemplate.delete(BLOCK_KEY + username);
    }

    public void invalidateRequestSent(String username) {
        redisTemplate.delete(REQUEST_SENT_KEY + username);
    }

    public void invalidateRequestReceived(String username) {
        redisTemplate.delete(REQUEST_RECEIVED_KEY + username);
    }

    public void removeIfInSuggestion(String username, String targetUsername) {
        String key = SUGGESTED_KEY + username;
        String reverseKey = SUGGESTED_KEY + targetUsername;
        redisTemplate.opsForSet().remove(key, targetUsername);
        redisTemplate.opsForSet().remove(reverseKey, username);
    }

    public boolean isBlocked(String username, String targetUsername) {
        String key = BLOCK_KEY + username;
        if (!redisTemplate.hasKey(key)) {
            saveToRedis(key, blockRepository.getBlockedUserUsernames(username, User.MAX_BLOCK_COUNT));
        }
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, targetUsername));
    }

    public boolean isFriend(String username, String targetUsername) {
        String key = FRIEND_KEY + username;
        String reverseKey = FRIEND_KEY + targetUsername;
        if (!redisTemplate.hasKey(key)) {
            saveToRedis(key, friendRepository.getFriendUsernames(username, User.MAX_FRIEND_COUNT));
        }

        if (!redisTemplate.hasKey(reverseKey)) {
            saveToRedis(reverseKey, friendRepository.getFriendUsernames(targetUsername, User.MAX_FRIEND_COUNT));
        }

        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, targetUsername))
                || Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(reverseKey, username));
    }

    private List<UserProjection> readOnCache(String key) {
        Set<String> members = redisTemplate.opsForSet()
                .members(key);
        if (members == null) {
            return Collections.emptyList();
        }
        return userRepository.getUsersByUsername(members);
    }

    private List<UserProjection> saveToRedis(String key, Set<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }

        if (key.startsWith(SUGGESTED_KEY)) {
            redisTemplate.opsForSet().add(key, usernames.toArray(new String[0]));
            redisTemplate.expire(key, SUGGESTED_VALIDITY_HOURS, java.util.concurrent.TimeUnit.HOURS);
        } else redisTemplate.opsForSet().add(key, usernames.toArray(new String[0]));
        return userRepository.getUsersByUsername(usernames);
    }

    private List<UserCommonInformationResponse> mapToResponse(List<UserProjection> projections) {
        return projections.stream()
                .map(userMapper::toUserCommonInformationResponse)
                .toList();
    }
}
