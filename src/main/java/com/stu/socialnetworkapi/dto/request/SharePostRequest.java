package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.enums.PostPrivacy;

import java.util.UUID;

public record SharePostRequest (
        String content,
        PostPrivacy privacy,
        UUID originalPostId
){
}
