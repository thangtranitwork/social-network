package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.Month;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StatisticsRepository {
    private final Neo4jClient neo4jClient;
    //user
    public UserStatisticsResponse getCommonUserStatistics(){
        return neo4jClient.query("""
                        MATCH (u:User)<-[:HAS_INFO]-(a:Account)
                        WITH date() AS today, u, a
                        Return Count(u) AS totalUsers,
                               Count(CASE WHEN a.isVerified = false THEN 1 END) AS notVerifiedUsers,
                               Count(CASE WHEN date(u.createdAt) = today THEN 1 END) AS newUsersToday,
                               Count(CASE WHEN u.createdAt.week=today.week AND u.createdAt.year=today.year THEN 1 END) AS newUsersThisWeek,
                               Count(CASE WHEN u.createdAt.month=today.month AND u.createdAt.year=today.year THEN 1 END) AS newUsersThisMonth,
                               Count(CASE WHEN u.createdAt.year=today.year THEN 1 END) AS newUsersThisYear
                        """)
                .fetchAs(UserStatisticsResponse.class)
                .mappedBy(((typeSystem, record) ->
                        UserStatisticsResponse.builder()
                        .totalUsers(record.get("totalUsers").asInt())
                                .notVerifiedUsers(record.get("notVerifiedUsers").asInt())
                                .newUsersToday(record.get("newUsersToday").asInt())
                                .newUsersThisWeek(record.get("newUsersThisWeek").asInt())
                                .newUsersThisMonth(record.get("newUsersThisMonth").asInt())
                                .newUsersThisYear(record.get("newUsersThisYear").asInt())

                        .build()))
                .one()
                .orElse(UserStatisticsResponse.builder().build());
    }
    public Map<DayOfWeek, Integer> getWeekUserStatistics(int week, int year){
        Map<DayOfWeek, Integer> stats = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            stats.put(day, 0);
        }

        neo4jClient.query("""
                MATCH (u:User)
                WHERE u.createdAt.week=$week AND u.createdAt.year=$year
                RETURN u.createdAt.dayOfWeek AS dayOfWeek, count(*) AS total
                """).bind(week).to("week")
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int dayOfWeek= ((Number)row.get("dayOfWeek")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(DayOfWeek.of(dayOfWeek),total);
                });

        return stats;
    }
    public Map<Integer, Integer> getMonthUserStatistics(int month, int year){
        int dayNum = 31;
        Map<Integer, Integer> stats = new HashMap<>();
        for (int i=1; i<=dayNum; i++){
            stats.put(i, 0);
        }
        neo4jClient.query("""
                MATCH (u:User)
                WHERE u.createdAt.year=$year AND u.createdAt.month=$month
                Return u.createdAt.day AS day, count(*) AS total
        """)
                .bind(month).to("month")
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int day = ((Number)row.get("day")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(day,total);
                });
        return stats;
    }
    public Map<Month, Integer> getYearUserStatistics(int year){

       Map<Month, Integer> stats = new EnumMap<>(Month.class);
        for (Month month : Month.values()) {
            stats.put(month, 0);
        }
        neo4jClient.query("""
                MATCH (u:User)
                WHERE u.createdAt.year=$year
                RETURN u.createdAt.month AS month, count(*) AS total
        """)
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int month = ((Number)row.get("month")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(Month.of(month),total);
                });
        return stats;
    }
    //post
    public PostStatisticsResponse getCommonPostStatistics(){
        return neo4jClient.query("""
                MATCH (post:Post)
                WITH date() AS today, post
                OPTIONAL MATCH (post)-[attach:ATTACH_FILES]->(:File)
                RETURN count(DISTINCT post) AS totalPosts,
                       sum(post.likeCount) AS totalLikes,
                       sum(post.commentCount) AS totalComments,
                       count(attach) AS totalFiles,
                       sum(post.shareCount) AS totalShares,
                       count(CASE WHEN post.privacy = "PUBLIC" THEN 1 END) AS publicPostCount,
                       count(CASE WHEN post.privacy = "FRIEND" THEN 1 END) AS friendPostCount,
                       count(CASE WHEN post.privacy = "PRIVATE" THEN 1 END) AS privatePostCount,
                       count(CASE WHEN post.deletedAT IS NOT NULL THEN 1 END) AS deletedPostCount,
                       count(CASE WHEN date(post.createdAt) = today THEN 1 END) AS newPostsToday,
                       count(CASE WHEN post.createdAt.week = today.week THEN 1 END) AS newPostsThisWeek,
                       count(CASE WHEN post.createdAt.month = today.month THEN 1 END) AS newPostsThisMonth,
                       count(CASE WHEN post.createdAt.year = today.year THEN 1 END) AS newPostsThisYear
                """)
                .fetchAs(PostStatisticsResponse.class)
                .mappedBy((typeSystem, record) ->
                        PostStatisticsResponse.builder()
                                .totalPosts(record.get("totalPosts").asInt())
                                .totalLikes(record.get("totalLikes").asInt())
                                .totalComments(record.get("totalComments").asInt())
                                .totalShares(record.get("totalShares").asInt())
                                .totalFiles(record.get("totalFiles").asInt())
                                .newPostsToday(record.get("newPostsToday").asInt())
                                .newPostsThisWeek(record.get("newPostsThisWeek").asInt())
                                .newPostsThisMonth(record.get("newPostsThisMonth").asInt())
                                .newPostsThisYear(record.get("newPostsThisYear").asInt())
                                .friendPostCount(record.get("friendPostCount").asInt())
                                .privatePostCount(record.get("privatePostCount").asInt())
                                .publicPostCount(record.get("publicPostCount").asInt())
                                .deletedPostCount(record.get("deletedPostCount").asInt())
                                .newPostsToday(record.get("newPostsToday").asInt())
                                .build())
                .one()
                .orElse(PostStatisticsResponse.builder().build());
    }
    public Map<DayOfWeek, Integer> getWeekPostStatistics(int week, int year){
        Map<DayOfWeek, Integer> stats = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            stats.put(day, 0);
        }
        neo4jClient.query("""
                MATCH (post:Post)
                WHERE post.createdAt.week=$week AND post.createdAt.year=$year
                RETURN post.createdAt.dayOfWeek AS dayOfWeek, count(*) AS total
                """)
                .bind(week).to("week")
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int dayOfWeek= ((Number)row.get("dayOfWeek")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(DayOfWeek.of(dayOfWeek),total);
                });
        return stats;
    }
    public Map<Integer, Integer> getMonthPostStatistics(int month, int year){
        int dayNum = 31;
        Map<Integer, Integer> stats = new HashMap<>();
        for (int i=1; i<=dayNum; i++){
            stats.put(i, 0);
        }
        neo4jClient.query("""
                MATCH (post:Post)
                WHERE post.createdAt.year=$year AND post.createdAt.month=$month
                Return post.createdAt.day AS day, count(*) AS total
        """)
                .bind(month).to("month")
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int day = ((Number)row.get("day")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(day,total);
                });
        return stats;
    }

    public Map<Month, Integer> getYearPostStatistics( int year){
        Map<Month, Integer> stats = new EnumMap<>(Month.class);
        for (Month month : Month.values()) {
            stats.put(month, 0);
        }
        neo4jClient.query("""
                MATCH (post:Post)
                WHERE post.createdAt.year=$year
                RETURN post.createdAt.month AS month, count(*) AS total
        """)
                .bind(year).to("year")
                .fetch()
                .all()
                .forEach(row->{
                    int month = ((Number)row.get("month")).intValue();
                    int total =((Number)row.get("total")).intValue();
                    stats.put(Month.of(month),total);
                });
        return stats;
    }


}
