package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.service.itf.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ApiResponse<Object> search(
            @NotBlank(message = "SEARCH_QUERY_REQUIRED") String query,
            @RequestParam(defaultValue = "NOT_SET") SearchService.SearchType type,
            Pageable pageable) {
        return ApiResponse.success(searchService.search(query, type, pageable.getPageNumber(), pageable.getPageSize()));
    }
}
