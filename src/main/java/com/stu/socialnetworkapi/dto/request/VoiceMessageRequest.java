package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Username;
import com.stu.socialnetworkapi.validation.annotation.ValidVoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record VoiceMessageRequest(
        @NotBlank(message = "MESSAGE_USERNAME_REQUIRED")
        @Username
        String username,
        @NotNull(message = "FILE_MESSAGE_REQUIRED")
        @ValidVoice
        MultipartFile voiceFile
) {

}
