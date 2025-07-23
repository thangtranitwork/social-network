package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PostStatisticsResponse {
    Integer totalPosts;
    Integer totalLikes;
    Integer totalComments;
    Integer totalShares;
    Integer totalFiles;

    Integer publicPostCount;
    Integer friendPostCount;
    Integer privatePostCount;
    Integer deletedPostCount;

    Integer newPostsToday;
    Integer newPostsThisWeek;
    Integer newPostsThisMonth;
    Integer newPostsThisYear;

    List<PostResponse> hottestTodayPosts;

    Map<DayOfWeek, Integer> thisWeekStatistics;
    Map<Integer, Integer> thisMonthStatistics;
    Map<Month, Integer> thisYearStatistics;
    Map<Year, Integer> allOfTimeStats;
}
