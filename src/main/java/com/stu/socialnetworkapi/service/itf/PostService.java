package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.request.PostUpdateContentRequest;
import com.stu.socialnetworkapi.dto.request.SharePostRequest;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.PostPrivacy;

import java.util.List;
import java.util.UUID;

public interface PostService {
    Post getPostById(UUID id);

    PostResponse get(UUID postId);

    List<PostResponse> getPostsOfUser(String authorUsername, Neo4jPageable pageable);

    List<PostResponse> getSuggestedPosts(Neo4jPageable pageable);

    PostResponse post(PostRequest request);

    PostResponse share(SharePostRequest request);

    void delete(UUID postId);

    void updatePrivacy(UUID postId, PostPrivacy privacy);

    void updateContent(UUID postId, PostUpdateContentRequest request);

    void like(UUID postId);

    void unlike(UUID postId);

    void validateViewPost(UUID postId, UUID userId);

    List<String> getFilesInPostsOfUser(String username, Neo4jPageable pageable);
}
