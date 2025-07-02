package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.entity.Keyword;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.service.itf.KeywordExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractorServiceImpl implements KeywordExtractorService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "http://localhost:8000/extract-keywords/";
    private final PostRepository postRepository;

    @Override
    public void extract(Post post) {
        if (post.getContent() == null || post.getContent().length() < 500) return;
        try {
            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", post.getContent());
            requestBody.put("top_n", 2);

            // Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gộp vào entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Gửi POST request
            ResponseEntity<String[]> response = restTemplate.postForEntity(API_URL, entity, String[].class);

            // Trả về danh sách từ khóa
            String[] body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                post.setKeywords(Arrays.stream(body)
                        .map(Keyword::new)
                        .collect(Collectors.toSet()));
                postRepository.save(post);
                System.out.print("Add keyword to post: " + post.getId() + ": ");
                post.getKeywords().forEach(System.out::println);
            }
        } catch (Exception e) {
            log.error("Error while call python service: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
