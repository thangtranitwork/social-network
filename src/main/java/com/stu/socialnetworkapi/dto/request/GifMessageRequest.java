package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.Username;
import jakarta.validation.constraints.NotBlank;

public record GifMessageRequest (
        @NotBlank(message = "MESSAGE_USERNAME_REQUIRED")
        @Username
        String username,
        @NotBlank(message = "GIF_URL_REQUIRED")
        String url
){
}
