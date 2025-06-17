package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.PostProjection;
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

    public PostResponse toPostResponse(PostProjection projection) {
        if (projection == null) {
            return null;
        }
        return PostResponse.builder()
                .id(projection.id())
                .author(userMapper.toPostAuthorCommonInformationResponse(projection))
                .content(projection.content())
                .files(File.getPathByIds(projection.files()))
                .likeCount(projection.likeCount())
                .shareCount(projection.shareCount())
                .commentCount(projection.commentCount())
                .createdAt(projection.createdAt())
                .updatedAt(projection.updatedAt())
                .privacy(projection.privacy())
                .originalPostCanView(projection.originalPostCanView())
                .originalPost(projection.originalPostCanView() && projection.originalPostAuthorId() != null ? this.toPostCommonInformationResponse(projection) : null)
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

    public PostCommonInformationResponse toPostCommonInformationResponse(PostProjection projection) {
        if (projection == null) {
            return null;
        }

        return PostCommonInformationResponse.builder()
                .id(projection.originalPostId())
                .author(userMapper.toOriginalPostAuthorCommonInformationResponse(projection))
                .content(projection.originalPostContent())
                .files(File.getPathByIds(projection.originalPostFiles()))
                .createdAt(projection.originalPostCreatedAt())
                .updatedAt(projection.originalPostUpdatedAt())
                .privacy(projection.originalPostPrivacy())
                .build();

    }
}
