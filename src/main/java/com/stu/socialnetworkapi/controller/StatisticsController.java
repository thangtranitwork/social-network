package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.service.itf.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/users")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        return ApiResponse.success(statisticsService.generateUserStatistics());
    }


    @GetMapping("/posts")
    public ApiResponse<PostStatisticsResponse> getPostStatistics() {
        return ApiResponse.success(statisticsService.generatePostStatistics());
    }
}
