package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.service.itf.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/files")
public class FileController {
    private final FileService fileService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> load(@PathVariable String id, @CookieValue(name = "token", required = false) String token){
        Resource resource = fileService.load(id, token);
        String contentType;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + fileService.getFilename(id) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
