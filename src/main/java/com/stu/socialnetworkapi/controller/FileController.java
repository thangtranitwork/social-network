package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.FileResponse;
import com.stu.socialnetworkapi.service.itf.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/files")
public class FileController {
    private final FileService fileService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> load(@PathVariable String id) {
        FileResponse file = fileService.load(id);
        String contentType = file.getContentType();
        boolean safeInline = contentType != null && (
                (contentType.startsWith("image/") && !contentType.equalsIgnoreCase("image/svg+xml"))
                        || contentType.startsWith("video/")
                        || contentType.startsWith("audio/")
                        || contentType.equalsIgnoreCase("application/pdf")
        );
        String dispositionType = safeInline ? "inline" : "attachment";
        String safeFileName = file.getName() != null ? file.getName().replaceAll("[\r\n\"]", "_") : id;
        return ResponseEntity.ok()
                .header("Content-Disposition", dispositionType + "; filename=\"" + safeFileName + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600, immutable") // Cache file trong 1 tieng
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(file.getResource());
    }
}
