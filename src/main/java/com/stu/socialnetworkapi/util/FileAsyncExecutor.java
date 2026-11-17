package com.stu.socialnetworkapi.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileAsyncExecutor {
    private static final String UPLOAD_DIRECTORY = "upload";

    private final Path root = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();

    @Async
    public void save(byte[] content, String newFileName) throws IOException {
        Path target = root.resolve(newFileName).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Invalid file path: " + newFileName);
        }
        Files.write(target, content);
    }
}
