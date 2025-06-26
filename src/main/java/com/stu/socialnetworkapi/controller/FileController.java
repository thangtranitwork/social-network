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
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600, immutable") // Cache file trong 1 tieng
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }
}
