package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.util.List;
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
    List<OnlineUserLog> onlineStatistics;
    Map<DayOfWeek, Integer> thisWeekStatistics;
    Map<Integer, Integer> thisMonthStatistics;
    Map<Month, Integer> thisYearStatistics;
    Map<Year, Integer> allOfTimeStatistics;
}
