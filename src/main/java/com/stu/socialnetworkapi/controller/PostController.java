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
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/posts")
public class PostController {
    private final PostService postService;

    @GetMapping("/newsfeed")
    public ApiResponse<Slice<PostResponse>> getPosts() {
        return ApiResponse.success(postService.getSuggestedPosts());
    }

    @GetMapping("/author/{authorUsername}")
    public ApiResponse<Slice<PostResponse>> getPosts(@PathVariable @Username String authorUsername, Pageable pageable) {
        return ApiResponse.success(postService.getPostsOfUser(authorUsername, pageable));
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

    @PostMapping("/pin/{postId}")
    public ApiResponse<Void> pinPost(@PathVariable UUID postId) {
        postService.pin(postId);
        return ApiResponse.success();
    }

    @PostMapping("/store/{postId}")
    public ApiResponse<Void> storePost(@PathVariable UUID postId) {
        postService.store(postId);
        return ApiResponse.success();
    }

    @PatchMapping("/update-privacy")
    public ApiResponse<Void> updatePostPrivacy(@RequestParam UUID id, @RequestParam PostPrivacy privacy) {
        postService.updatePrivacy(id, privacy);
        return ApiResponse.success();
    }

    @PatchMapping("/update-content")
    public ApiResponse<PostResponse> updatePostContent(@RequestParam UUID id, @Valid PostUpdateContentRequest request) {
        return ApiResponse.success(postService.updateContent(id, request));
    }

    @DeleteMapping("/unlike/{postId}")
    public ApiResponse<Void> unlikePost(@PathVariable UUID postId) {
        postService.unlike(postId);
        return ApiResponse.success();
    }

    @DeleteMapping("/unpin")
    public ApiResponse<Void> unpinPost() {
        postService.unpin();
        return ApiResponse.success();
    }

}
