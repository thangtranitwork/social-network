package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.enums.PostPrivacy;
import com.stu.socialnetworkapi.validation.annotation.ImagesAndVideosOnly;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostRequest(
        String content,
        PostPrivacy privacy,
        @ImagesAndVideosOnly
        List<MultipartFile> files
) {
}
