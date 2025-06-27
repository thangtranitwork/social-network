package com.stu.socialnetworkapi.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class PostResponse extends PostCommonInformationResponse {
    int likeCount;
    int shareCount;
    int commentCount;
    boolean liked;
    boolean isSharedPost;
    boolean originalPostCanView;
    PostCommonInformationResponse originalPost;
    Double score;
}
