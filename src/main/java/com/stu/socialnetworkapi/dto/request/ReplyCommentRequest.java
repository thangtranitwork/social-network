package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.ImageAndVideoOnly;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record ReplyCommentRequest(
        String content,
        @ImageAndVideoOnly
        MultipartFile file,
        @NotNull(message = "ORIGINAL_COMMENT_ID_REQUIRED")
        UUID originalCommentId
) {
}
