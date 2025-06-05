package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.CommentResponse;
import com.stu.socialnetworkapi.service.itf.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/of-post/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId, Pageable pageable) {
        return ApiResponse.success(commentService.getComments(postId, pageable));
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
