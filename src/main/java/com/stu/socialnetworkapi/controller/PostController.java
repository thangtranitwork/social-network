package com.stu.socialnetworkapi.controller;

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
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/posts")
public class PostController {
    private final PostService postService;

    @GetMapping("/newsfeed")
    public ApiResponse<List<PostResponse>> getPosts(Pageable pageable) {
        return ApiResponse.success(postService.getSuggestedPosts(pageable));
    }

    @GetMapping("/of-user/{username}")
    public ApiResponse<List<PostResponse>> getPosts(@PathVariable @Username String username, Pageable pageable) {
        return ApiResponse.success(postService.getPostsOfUser(username, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPost(@PathVariable UUID id) {
        return ApiResponse.success(postService.get(id));
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
    public ApiResponse<PostResponse> updatePostContent(@PathVariable UUID postId, @Valid PostUpdateContentRequest request) {
        return ApiResponse.success(postService.updateContent(postId, request));
    }

    @DeleteMapping("/unlike/{postId}")
    public ApiResponse<Void> unlikePost(@PathVariable UUID postId) {
        postService.unlike(postId);
        return ApiResponse.success();
    }

}
