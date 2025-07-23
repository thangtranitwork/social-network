package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

public interface StatisticsServiceV2 {
    UserStatisticsResponse generateUserStatistics();

    PostStatisticsResponse generatePostStatistics();

    Map<DayOfWeek, Integer> generateWeeklyUserStatistics(String weekString);

    Map<Integer, Integer> generateMonthlyUserStatistics(String monthString);

    Map<Month, Integer> generateYearlyUserStatistics(Year year);

    List<OnlineUserLog> generateOnlineUserCount(LocalDate day);

    Map<DayOfWeek, Integer> generateWeeklyPostStatistics(String weekString);

    Map<Integer, Integer> generateMonthlyPostStatistics(String monthString);

    Map<Month, Integer> generateYearlyPostStatistics(Year year);
}
