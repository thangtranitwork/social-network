package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.response.CommentResponse;
import com.stu.socialnetworkapi.entity.Comment;
import com.stu.socialnetworkapi.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final UserMapper userMapper;

    public CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(userMapper.toUserCommonInformationResponse(comment.getAuthor()))
                .fileUrl(File.getPath(comment.getAttachedFile()))
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
