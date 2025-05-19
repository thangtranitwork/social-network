package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.web.multipart.MultipartFile;

public record PostRequest(
        String content,
        PostPrivacy privacy,
        MultipartFile files
) {
}
