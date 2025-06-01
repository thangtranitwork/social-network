package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.CommentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface CommentService {
    CommentResponse comment(CommentRequest request);

    CommentResponse reply(ReplyCommentRequest request);

    void like(UUID id);

    void unlike(UUID id);

    void delete(UUID id);

    Slice<CommentResponse> getComments(UUID postId, Pageable pageable);
}
