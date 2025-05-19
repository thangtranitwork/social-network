package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface PostService {
    PostResponse get(UUID postId);

    Slice<PostResponse> getSuggestedPosts();

    PostResponse post(PostRequest request);

    PostResponse share(PostRequest request);

    void delete(UUID postId);

    void updatePrivacy(UUID postId, PostPrivacy privacy);

    PostResponse updateContent(UUID postId, PostRequest request);

    void pin(UUID postId);

    void unpin();

    void store(UUID postId);

    void like(UUID postId);

    void unlike(UUID postId);
}
