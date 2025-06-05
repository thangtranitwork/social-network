package com.stu.socialnetworkapi.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileAsyncExecutor {
    private static final String UPLOAD_DIRECTORY = "upload";

    private final Path root = Paths.get(UPLOAD_DIRECTORY);
    @Async
    public void save(MultipartFile file, String newFileName) throws IOException {
        Files.copy(file.getInputStream(), root.resolve(newFileName));
    }
}
