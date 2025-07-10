package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.CommentResponse;
import com.stu.socialnetworkapi.service.itf.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/of-post/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId, Neo4jPageable pageable) {
        return ApiResponse.success(commentService.getComments(postId, pageable));
    }

    @GetMapping("/of-comment/{commentId}")
    public ApiResponse<List<CommentResponse>> getCommentsByOriginalComment(@PathVariable UUID commentId, Neo4jPageable pageable) {
        return ApiResponse.success(commentService.getRepliedComments(commentId, pageable));
    }

    @PostMapping
    public ApiResponse<CommentResponse> comment(@Valid CommentRequest request) {
        return ApiResponse.success(commentService.comment(request));
    }

    @PostMapping("/reply")
    public ApiResponse<CommentResponse> reply(@Valid ReplyCommentRequest request) {
        return ApiResponse.success(commentService.reply(request));
    }

    @PostMapping("/like/{commentId}")
    public ApiResponse<Void> like(@PathVariable UUID commentId) {
        commentService.like(commentId);
        return ApiResponse.success();
    }

    @PatchMapping("{id}")
    public ApiResponse<CommentResponse> updateContent(@PathVariable UUID id, @RequestBody CommentRequest request) {
        return ApiResponse.success(commentService.updateContent(id, request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ApiResponse.success();
    }

    @DeleteMapping("/unlike/{commentId}")
    public ApiResponse<Void> unlike(@PathVariable UUID commentId) {
        commentService.unlike(commentId);
        return ApiResponse.success();
    }
}
