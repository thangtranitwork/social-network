package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.PostResponse;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.mapper.PostMapper;
import com.stu.socialnetworkapi.repository.IsOnlineRedisRepository;
import com.stu.socialnetworkapi.repository.PostRepository;
import com.stu.socialnetworkapi.repository.UserRepository;
import com.stu.socialnetworkapi.service.itf.StatisticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final IsOnlineRedisRepository isOnlineRedisRepository;
    private static final Integer DEFAULT_VALUE = null;

    @Override
    public UserStatisticsResponse generateUserStatistics() {
        LocalDate now = LocalDate.now();
        ZonedDateTime startOfWeek = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime startOfMonth = now
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault());
        DayOfWeek todayDayOfWeek = now.getDayOfWeek();
        Month todayMonth = now.getMonth();
        int todayDayOfMonth = now.getDayOfMonth();
        int thisYear = now.getYear();
        UserStatisticsResponse response = userRepository.getCommonUserStatistics(startOfWeek);
        response.setOnlineUsersNow(isOnlineRedisRepository.countOnlineUsers());

        Map<DayOfWeek, Integer> thisWeekStats = new EnumMap<>(DayOfWeek.class);
        Arrays.stream(DayOfWeek.values())
                .forEach(dayOfWeek -> thisWeekStats.put(dayOfWeek, DEFAULT_VALUE));
        userRepository.getThisWeekStatistics(startOfWeek)
                .stream()
                .filter(data -> data.key() <= todayDayOfWeek.getValue())
                .forEach(data -> thisWeekStats.put(DayOfWeek.of(data.key()), data.count()));
        response.setThisWeekStatistics(thisWeekStats);

        Map<Integer, Integer> thisMonthStats = new HashMap<>();
        for (int i = 1; i <= todayDayOfMonth; i++) {
            thisMonthStats.put(i, DEFAULT_VALUE);
        }

        userRepository.getThisMonthStatistics(startOfMonth, todayDayOfMonth)
                .forEach(data -> thisMonthStats.put(data.key(), data.count()));
        response.setThisMonthStatistics(thisMonthStats);

        Map<Month, Integer> monthStats = new EnumMap<>(Month.class);
        Arrays.stream(Month.values()).forEach(month -> monthStats.put(month, DEFAULT_VALUE));
        userRepository.getThisYearStatistics(thisYear)
                .stream()
                .filter(data -> data.key() <= todayMonth.getValue())
                .forEach(data -> monthStats.put(Month.of(data.key()), data.count()));

        response.setThisYearStatistics(monthStats);

        Map<Year, Integer> yearStats = new HashMap<>();
        userRepository.getAllTimeYearlyStatistics()
                .forEach(data -> yearStats.put(Year.of(data.key()), data.count()));
        response.setAllOfTimeStatistics(yearStats);

        return response;
    }

    @Override
    public PostStatisticsResponse generatePostStatistics() {
        LocalDate now = LocalDate.now();
        ZonedDateTime today = now.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime startOfWeek = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime startOfMonth = now
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault());

        DayOfWeek todayDayOfWeek = now.getDayOfWeek();
        Month todayMonth = now.getMonth();
        int todayDayOfMonth = now.getDayOfMonth();
        int thisYear = now.getYear();

        PostStatisticsResponse response = postRepository.getCommonPostStatistics(startOfWeek);

        List<Post> hottestPosts = postRepository.findAllById(postRepository.getHottestPostsToday(today, 10));
        List<PostResponse> hottestPostResponses = hottestPosts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        response.setHottestTodayPosts(hottestPostResponses);

        Map<DayOfWeek, Integer> thisWeekStats = new EnumMap<>(DayOfWeek.class);
        Arrays.stream(DayOfWeek.values())
                .forEach(dayOfWeek -> thisWeekStats.put(dayOfWeek, DEFAULT_VALUE));
        postRepository.getThisWeekStatistics(startOfWeek)
                .stream()
                .filter(data -> data.key() <= todayDayOfWeek.getValue())
                .forEach(data -> thisWeekStats.put(DayOfWeek.of(data.key()), data.count()));
        response.setThisWeekStatistics(thisWeekStats);

        Map<Integer, Integer> thisMonthStats = new HashMap<>();
        for (int i = 1; i <= todayDayOfMonth; i++) {
            thisMonthStats.put(i, DEFAULT_VALUE);
        }
        postRepository.getThisMonthStatistics(startOfMonth, todayDayOfMonth)
                .forEach(data -> thisMonthStats.put(data.key(), data.count()));
        response.setThisMonthStatistics(thisMonthStats);

        Map<Month, Integer> monthStats = new EnumMap<>(Month.class);
        Arrays.stream(Month.values()).forEach(month -> monthStats.put(month, DEFAULT_VALUE));
        postRepository.getThisYearStatistics(thisYear)
                .stream()
                .filter(data -> data.key() <= todayMonth.getValue())
                .forEach(data -> monthStats.put(Month.of(data.key()), data.count()));
        response.setThisYearStatistics(monthStats);

        Map<Year, Integer> yearStats = new HashMap<>();
        postRepository.getAllTimeYearlyStatistics()
                .forEach(data -> yearStats.put(Year.of(data.key()), data.count()));
        response.setAllOfTimeStats(yearStats);

        return response;
    }
}
