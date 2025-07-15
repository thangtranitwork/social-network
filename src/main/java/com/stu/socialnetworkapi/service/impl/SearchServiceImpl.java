package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.projection.PostProjection;
import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.mapper.PostMapper;
import com.stu.socialnetworkapi.mapper.UserMapper;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.repository.UserRepository;
import com.stu.socialnetworkapi.service.itf.SearchService;
import com.stu.socialnetworkapi.service.itf.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public Object search(String query, SearchType type, long skip, long limit) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        Map<SearchType, List<?>> result = new EnumMap<>(SearchType.class);
        switch (type) {
            case NOT_SET -> {
                List<UserProjection> users = userRepository.fullTextSearch(query, currentUserId, limit, 0);
                List<PostProjection> posts = postRepository.fullTextSearch(query, currentUserId, limit, 0);
                result.put(SearchType.USER, users.stream()
                        .map(userMapper::toUserCommonInformationResponse)
                        .toList());
                result.put(SearchType.POST, posts.stream()
                        .map(postMapper::toPostResponse)
                        .toList());
                return result;
            }
            case USER -> {
                List<UserProjection> users = userRepository.fullTextSearch(query, currentUserId, limit, skip);
                result.put(SearchType.USER, users.stream()
                        .map(userMapper::toUserCommonInformationResponse)
                        .toList());
                result.put(SearchType.POST, null);
                return result;
            }
            case POST -> {
                List<PostProjection> posts = postRepository.fullTextSearch(query, currentUserId, limit, skip);
                result.put(SearchType.POST, posts.stream()
                        .map(postMapper::toPostResponse)
                        .toList());
                result.put(SearchType.USER, null);
                return result;
            }
        }
        return result;
    }
}
