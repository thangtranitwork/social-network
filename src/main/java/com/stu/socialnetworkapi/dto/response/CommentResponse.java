package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    UUID id;
    String content;
    int likeCount;
    int replyCount;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;
    UserCommonInformationResponse author;
    String fileUrl;
}
