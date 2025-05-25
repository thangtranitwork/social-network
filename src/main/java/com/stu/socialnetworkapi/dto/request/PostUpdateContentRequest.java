package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.ValidFileList;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostUpdateContentRequest(
        String content,
        @ValidFileList
        List<MultipartFile> newFiles,
        List<String> deleteOldFileUrls
) {
}
