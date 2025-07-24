package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.entity.sqlite.OnlineUserLog;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.StatisticsRepository;
import com.stu.socialnetworkapi.repository.redis.IsOnlineRepository;
import com.stu.socialnetworkapi.repository.sqlite.OnlineUserLogRepository;
import com.stu.socialnetworkapi.service.itf.StatisticsServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImplV2 implements StatisticsServiceV2{
    private final StatisticsRepository statisticsRepository;
    private final IsOnlineRepository isOnlineRepository;
    private final OnlineUserLogRepository  onlineUserLogRepository;
    //Get Common User Statistic
    @Override
    public UserStatisticsResponse generateUserStatistics() {
        int week=LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        int year=LocalDate.now().getYear();
        int month=LocalDate.now().getMonthValue();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.minusHours(now.getHour())
                .minusMinutes(now.getMinute())
                .minusSeconds(now.getSecond());
        UserStatisticsResponse response=statisticsRepository.getCommonUserStatistics();
        response.setOnlineUsersNow(isOnlineRepository.countOnlineUsers());
        response.setThisWeekStatistics(statisticsRepository.getWeekUserStatistics(week, year));
        response.setThisMonthStatistics(statisticsRepository.getMonthUserStatistics(month, year));
        response.setThisYearStatistics(statisticsRepository.getYearUserStatistics(year));
        response.setOnlineStatistics(onlineUserLogRepository.findByTimestampBetween(startOfDay, now));
        return response;
    }

    @Override
    public PostStatisticsResponse generatePostStatistics() {
        LocalDate now = LocalDate.now();
        int week= now.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year= now.getYear();
        int month= now.getMonthValue();
        PostStatisticsResponse response = statisticsRepository.getCommonPostStatistics();
        response.setThisWeekStatistics(statisticsRepository.getWeekPostStatistics(week, year));
        response.setThisMonthStatistics(statisticsRepository.getMonthPostStatistics(month, year));
        response.setThisYearStatistics(statisticsRepository.getYearPostStatistics(year));
        return response;
    }

    @Override
    public Map<DayOfWeek, Integer> generateWeeklyUserStatistics(String weekString) {
        try {
            int week = Integer.parseInt(weekString.substring(6));
            int year = Integer.parseInt(weekString.substring(0, 4));
            return statisticsRepository.getWeekUserStatistics(week, year);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public Map<Integer, Integer> generateMonthlyUserStatistics(String monthString) {
        try {
            int month = Integer.parseInt(monthString.substring(5));
            int year = Integer.parseInt(monthString.substring(0, 4));
            return statisticsRepository.getMonthUserStatistics(month, year);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public Map<Month, Integer> generateYearlyUserStatistics(Year year) {
        try {
            int thisYear = Integer.parseInt(String.valueOf(year));
            return statisticsRepository.getYearUserStatistics(thisYear);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public List<OnlineUserLog> generateOnlineUserCount(LocalDate day) {
        try {
            LocalDateTime startOfDay=day.atStartOfDay();
            LocalDateTime endOfDay=day.plusDays(1).atStartOfDay().minusSeconds(1);
            return onlineUserLogRepository.findByTimestampBetween(startOfDay, endOfDay);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public Map<DayOfWeek, Integer> generateWeeklyPostStatistics(String weekString) {
        try {
            int week = Integer.parseInt(weekString.substring(6));
            int year = Integer.parseInt(weekString.substring(0, 4));
            return statisticsRepository.getWeekPostStatistics(week, year);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public Map<Integer, Integer> generateMonthlyPostStatistics(String monthString) {
        try {
            int month = Integer.parseInt(monthString.substring(5));
            int year = Integer.parseInt(monthString.substring(0, 4));
            return statisticsRepository.getMonthPostStatistics(month, year);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public Map<Month, Integer> generateYearlyPostStatistics(Year year) {
        try {
            int thisYear = Integer.parseInt(String.valueOf(year));
            return statisticsRepository.getYearUserStatistics(thisYear);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
    }
}
