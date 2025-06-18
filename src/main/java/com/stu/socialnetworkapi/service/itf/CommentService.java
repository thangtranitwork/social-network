package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.request.CommentRequest;
import com.stu.socialnetworkapi.dto.request.Neo4jPageable;
import com.stu.socialnetworkapi.dto.request.ReplyCommentRequest;
import com.stu.socialnetworkapi.dto.response.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse comment(CommentRequest request);

    CommentResponse reply(ReplyCommentRequest request);

    void like(UUID id);

    void unlike(UUID id);

    void delete(UUID id);

    List<CommentResponse> getComments(UUID postId, Neo4jPageable pageable);

    List<CommentResponse> getRepliedComments(UUID commentId, Neo4jPageable pageable);
}
