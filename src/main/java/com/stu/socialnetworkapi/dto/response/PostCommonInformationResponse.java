package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.PostPrivacy;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
public class PostCommonInformationResponse {
    UUID id;
    String content;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;
    PostPrivacy privacy;
    UserCommonInformationResponse author;
    List<String> files;
}
