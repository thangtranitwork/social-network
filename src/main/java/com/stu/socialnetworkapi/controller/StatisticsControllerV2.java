package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import com.stu.socialnetworkapi.service.itf.StatisticsServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/statistics")
public class StatisticsControllerV2 {
    private final StatisticsServiceV2 statisticsService;
    //user
    @GetMapping("/users")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        return ApiResponse.success(statisticsService.generateUserStatistics());
    }
    @GetMapping("/users/week")
    public ApiResponse<Map<DayOfWeek, Integer>> getWeekUserStatistics(@RequestParam(name="week") String weekString) {
        return ApiResponse.success(statisticsService.generateWeeklyUserStatistics(weekString));
    }
    @GetMapping("/users/month")
    public ApiResponse<Map<Integer, Integer>> getMonthUserStatistics(@RequestParam(name="month") String monthString) {
        return ApiResponse.success(statisticsService.generateMonthlyUserStatistics(monthString));
    }

    @GetMapping("/users/year")
    public ApiResponse<Map<Month, Integer>> getYearUserStatistics(@RequestParam(name="year")Year yearString) {
        return ApiResponse.success(statisticsService.generateYearlyUserStatistics(yearString));
    }
    @GetMapping("/users/online")
    public ApiResponse<List<OnlineUserLog>> getOnlineUserCount(@RequestParam(name="date") LocalDate date){
        return ApiResponse.success(statisticsService.generateOnlineUserCount(date));
    }
    //post
    @GetMapping("/posts")
    public ApiResponse<PostStatisticsResponse> getPostStatistics() {
        return ApiResponse.success(statisticsService.generatePostStatistics());
    }
    @GetMapping("/posts/week")
    public ApiResponse<Map<DayOfWeek, Integer>> getWeekPostStatistics(@RequestParam(name="week") String weekString) {
        return ApiResponse.success(statisticsService.generateWeeklyPostStatistics(weekString));
    }
    @GetMapping("/posts/month")
    public ApiResponse<Map<Integer, Integer>> getMonthPostStatistics(@RequestParam(name="month") String monthString) {
        return ApiResponse.success(statisticsService.generateMonthlyPostStatistics(monthString));
    }

    @GetMapping("/posts/year")
    public ApiResponse<Map<Month, Integer>> getYearPostStatistics(@RequestParam(name="year") Year yearString) {
        return ApiResponse.success(statisticsService.generateYearlyPostStatistics(yearString));
    }
}
