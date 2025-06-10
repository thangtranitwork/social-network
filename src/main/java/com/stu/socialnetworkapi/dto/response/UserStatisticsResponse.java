package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Map;

@Data
@Builder
public class UserStatisticsResponse {
    Integer totalUsers;
    Integer notVerifiedUsers;
    Integer newUsersToday;
    Integer newUsersThisWeek;
    Integer newUsersThisMonth;
    Integer newUsersThisYear;
    Integer onlineUsersNow;
    Map<DayOfWeek, Integer> thisWeekStatistics;
    Map<Integer, Integer> thisMonthStatistics;
    Map<Month, Integer> thisYearStatistics;
    Map<Year, Integer> allOfTimeStatistics;
    Map<LocalDate, Integer> usersOnlineStatistics;
}
