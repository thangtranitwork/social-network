package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.request.PostUpdateContentRequest;
import com.stu.socialnetworkapi.dto.request.SharePostRequest;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface PostService {
    Post getPostById(UUID id);

    PostResponse get(UUID postId);

    Slice<PostResponse> getPostsOfUser(String authorUsername, Pageable pageable);

    Slice<PostResponse> getSuggestedPosts();

    PostResponse post(PostRequest request);

    PostResponse share(SharePostRequest request);

    void delete(UUID postId);

    void updatePrivacy(UUID postId, PostPrivacy privacy);

    PostResponse updateContent(UUID postId, PostUpdateContentRequest request);

    void pin(UUID postId);

    void unpin();

    void store(UUID postId);

    void like(UUID postId);

    void unlike(UUID postId);

    void validateViewPost(UUID postId, UUID userId);
}
