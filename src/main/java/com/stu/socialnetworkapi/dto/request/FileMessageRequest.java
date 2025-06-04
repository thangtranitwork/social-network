package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.ValidFile;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record FileMessageRequest(
        UUID chatId,
        UUID userId,
        @NotNull(message = "FILE_MESSAGE_REQUIRED")
        @ValidFile
        MultipartFile attachment
) {
}
