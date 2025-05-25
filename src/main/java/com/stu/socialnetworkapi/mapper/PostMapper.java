package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.response.PostCommonInformationResponse;
import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final UserMapper userMapper;

    public PostResponse toPostResponse(Post post) {
        if (post == null) {
            return null;
        }
        return PostResponse.builder()
                .id(post.getId())
                .author(userMapper.toUserCommonInformationResponse(post.getAuthor()))
                .content(post.getContent())
                .files(File.getPath(post.getAttachedFiles()))
                .likeCount(post.getLikeCount())
                .shareCount(post.getShareCount())
                .commentCount(post.getCommentCount())
                .originalPost(this.toPostCommonInformationResponse(post.getOriginalPost()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .privacy(post.getPrivacy())
                .build();
    }

    public PostCommonInformationResponse toPostCommonInformationResponse(Post post) {
        if (post == null) {
            return null;
        }

        return PostCommonInformationResponse.builder()
                .id(post.getId())
                .author(userMapper.toUserCommonInformationResponse(post.getAuthor()))
                .content(post.getContent())
                .files(File.getPath(post.getAttachedFiles()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .privacy(post.getPrivacy())
                .build();

    }
}
