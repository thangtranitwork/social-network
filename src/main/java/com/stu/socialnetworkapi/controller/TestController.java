package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test")
public class TestController {
    private final KeywordExtractorService keywordExtractorService;

    @PostMapping
    public List<String> test(@RequestBody String prompt) {
        return keywordExtractorService.extract(prompt);
    }
}
