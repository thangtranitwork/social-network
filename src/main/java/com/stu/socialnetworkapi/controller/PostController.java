package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.PostRequest;
import com.stu.socialnetworkapi.dto.request.PostUpdateContentRequest;
import com.stu.socialnetworkapi.dto.request.SharePostRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import com.stu.socialnetworkapi.service.itf.PostService;
import com.stu.socialnetworkapi.validation.annotation.Username;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/posts")
public class PostController {
    private final PostService postService;

    @GetMapping("/newsfeed")
    public ApiResponse<List<PostResponse>> getPosts(Neo4jPageable pageable) {
        System.out.println("getPosts");
        return ApiResponse.success(postService.getSuggestedPosts(pageable));
    }

    @GetMapping("/of-user/{username}")
    public ApiResponse<List<PostResponse>> getPosts(@PathVariable @Username String username, Neo4jPageable pageable) {
        return ApiResponse.success(postService.getPostsOfUser(username, pageable));
    }

    @GetMapping("/files/{username}")
    public ApiResponse<List<String>> getFiles(@PathVariable @Username String username, Neo4jPageable pageable) {
        return ApiResponse.success(postService.getFilesInPostsOfUser(username, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPost(@PathVariable UUID id) {
        return ApiResponse.success(postService.get(id));
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> getPostsOfUser(Neo4jPageable pageable) {
        return ApiResponse.success(postService.getAllPosts(pageable));
    }

    @PostMapping("/post")
    public ApiResponse<PostResponse> createPost(@Valid PostRequest postRequest) {
        return ApiResponse.success(postService.post(postRequest));
    }

    @PostMapping("/share")
    public ApiResponse<PostResponse> sharePost(@Valid @RequestBody SharePostRequest sharePostRequest) {
        return ApiResponse.success(postService.share(sharePostRequest));
    }

    @PostMapping("/like/{postId}")
    public ApiResponse<Void> likePost(@PathVariable UUID postId) {
        postService.like(postId);
        return ApiResponse.success();
    }

    @PatchMapping("/update-privacy/{postId}")
    public ApiResponse<Void> updatePostPrivacy(@PathVariable UUID postId, @RequestParam PostPrivacy privacy) {
        postService.updatePrivacy(postId, privacy);
        return ApiResponse.success();
    }

    @PatchMapping("/update-content/{postId}")
    public ApiResponse<Void> updatePostContent(@PathVariable UUID postId, @Valid PostUpdateContentRequest request) {
        postService.updateContent(postId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/unlike/{postId}")
    public ApiResponse<Void> unlikePost(@PathVariable UUID postId) {
        postService.unlike(postId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable UUID postId) {
        postService.delete(postId);
        return ApiResponse.success();
    }
}
