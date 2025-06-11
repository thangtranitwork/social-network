package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.CountDataProjection;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends Neo4jRepository<Post, UUID> {
    List<Post> findAllByAuthorIdAndPrivacyIsIn(UUID authorId, Collection<PostPrivacy> privacies, Pageable pageable);

    @Query("""
            MATCH (p:Post {id: $postId})<-[like:LIKED]-(u:User {id: $likerId})
            RETURN COUNT(like) > 0
            """)
    boolean isLiked(UUID postId, UUID likerId);

    @Query("""
                MATCH (me:User {id: $userId})
                CALL db.index.fulltext.queryNodes("postSearchIndex", $query + "*")
                YIELD node AS post, score
                MATCH (author:User)-[:POSTED]->(post)
                WHERE NOT (author)-[:BLOCKED]->(me)
                  AND NOT (me)-[:BLOCKED]->(author)
                RETURN post.id
                ORDER BY score DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<UUID> fullTextSearch(String query, UUID userId, int limit, int skip);

    /**
     * Hệ thống chấm điểm
     * - Bài viết mới 24 giờ: 200 điểm
     * - Bài viết của bạn user: 100 điểm
     * - Bài viết của bạn của bạn của user: 50 điểm
     * - Độ dài đường đi ngắn nhất từ user đến bài viết >= 2: 120 / độ dài
     * - user xem trang cá nhân của tác giả: 2 điểm * số lần
     * - author xem trang cá nhân user: 1 điểm * số lần
     * - Số like: 2 điểm
     * - Số comment: 3 điểm
     * - Số share: 5 điểm
     * Cần kiểm tra privacy
     */

    @Query("""
                MATCH (u:User {id: $userId})
            MATCH (author:User)-[:POSTED]->(post:Post)
            WHERE (
                post.privacy = 'PUBLIC'
                OR (post.privacy = 'FRIEND' AND (u)-[:FRIEND]-(author))
            )
            AND NOT (u)-[:BLOCK]-(author)
            
            OPTIONAL MATCH p = shortestPath((u)-[*1..4]->(post))
            
            OPTIONAL MATCH (u)-[vu:VIEW_PROFILE]->(author)
            OPTIONAL MATCH (author)-[uv:VIEW_PROFILE]->(u)
            
            WITH u, post, author,
                 CASE WHEN p IS NULL THEN NULL ELSE length(p) END AS shortestPathLength,
                 coalesce(vu.times, 0) AS viewForward,
                 coalesce(uv.times, 0) AS viewBackward
            
            WITH post, author, u, shortestPathLength, viewForward, viewBackward,
            
                 CASE WHEN post.createdAt > datetime() - duration('P1D') THEN 200 ELSE 0 END AS newPostScore,
            
                 CASE
                     WHEN (u)-[:FRIEND]-(author) THEN 100
                     WHEN (u)-[:FRIEND]-()-[:FRIEND]-(author) AND NOT (u)-[:FRIEND]-(author) OR (u)-[:REQUEST]-(author) THEN 50
                     ELSE 0
                 END
                 + 2 * viewForward
                 + 1 * viewBackward AS relationshipScore,
            
                 CASE
                     WHEN shortestPathLength IS NULL OR shortestPathLength = 1 THEN 0
                     ELSE 120.0 / shortestPathLength
                 END AS pathScore,
            
                 post.likeCount * 2 AS likeScore,
                 post.commentCount * 3 AS commentScore,
                 post.shareCount * 5 AS shareScore
            
                WITH post,
                     pathScore + newPostScore + relationshipScore + likeScore + commentScore + shareScore AS totalScore
            
                ORDER BY totalScore DESC, post.createdAt DESC
                RETURN post.id AS id
                SKIP $skip
                LIMIT $limit
            """)
    List<UUID> getSuggestedPosts(UUID userId, Pageable pageable);

    @Query("""
            WITH datetime() AS today
            WITH today,
                 datetime({year: today.year, month: today.month, day: 1}) AS startOfMonth,
                 datetime({year: today.year, month: 1, day: 1}) AS startOfYear,
                 datetime($startOfWeek) AS startOfWeek
            
            MATCH (post:Post)
            OPTIONAL MATCH (post)-[attach:ATTACH_FILES]->(:File)
            
            RETURN
              count(post) AS totalPosts,
              sum(post.likeCount) AS totalLikes,
              sum(post.commentCount) AS totalComments,
              sum(post.shareCount) AS totalShares,
              count(attach) AS totalFiles,
              count(CASE WHEN post.createdAt >= today THEN 1 END) AS newPostsToday,
              count(CASE WHEN post.createdAt >= startOfWeek THEN 1 END) AS newPostsThisWeek,
              count(CASE WHEN post.createdAt >= startOfMonth THEN 1 END) AS newPostsThisMonth,
              count(CASE WHEN post.createdAt >= startOfYear THEN 1 END) AS newPostsThisYear
            """)
    PostStatisticsResponse getCommonPostStatistics(ZonedDateTime startOfWeek);

    // Get hottest posts today
    @Query("""
            MATCH (post:Post)
            WHERE date(post.createdAt) = date($today)
            WITH post, (post.likeCount * 2 + post.commentCount * 3 + post.shareCount * 5) AS score
            RETURN post.id
            ORDER BY score DESC
            LIMIT $limit
            """)
    List<UUID> getHottestPostsToday(ZonedDateTime today, int limit);

    // Weekly statistics (cumulative)
    @Query("""
            WITH range(1, 7) AS dayNumbers
            UNWIND dayNumbers AS dow
            WITH dow, datetime($startOfWeek) + duration({days: dow - 1, hours: 23, minutes: 59, seconds: 59}) AS endOfDay
            MATCH (post:Post)
            WHERE post.createdAt <= endOfDay
            WITH dow, count(post) AS count
            RETURN dow AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisWeekStatistics(ZonedDateTime startOfWeek);

    // Monthly statistics (cumulative)
    @Query("""
            WITH range(1, $daysInMonth) AS dayNumbers
            UNWIND dayNumbers AS dayNum
            WITH dayNum,
                 datetime($startOfMonth) + duration({days: dayNum - 1, hours: 23, minutes: 59, seconds: 59}) AS endOfDay
            MATCH (post:Post)
            WHERE post.createdAt <= endOfDay
            WITH dayNum, count(post) AS count
            RETURN dayNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisMonthStatistics(ZonedDateTime startOfMonth, int daysInMonth);

    // Yearly statistics (cumulative)
    @Query("""
            WITH range(1, 12) AS monthNumbers
            UNWIND monthNumbers AS monthNum
            WITH monthNum,
                 datetime({year: $year, month: monthNum, day: 1}) + duration({months: 1, seconds: -1}) AS endOfMonth
            MATCH (post:Post)
            WHERE post.createdAt <= endOfMonth
            WITH monthNum, count(post) AS count
            RETURN monthNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getThisYearStatistics(int year);

    // All time yearly statistics (cumulative)
    @Query("""
            MATCH (post:Post)
            WITH min(post.createdAt.year) AS minYear, max(post.createdAt.year) AS maxYear
            WITH range(minYear, maxYear) AS yearNumbers
            UNWIND yearNumbers AS yearNum
            WITH yearNum,
                 datetime({year: yearNum + 1, month: 1, day: 1}) + duration({seconds: -1}) AS endOfYear
            MATCH (post:Post)
            WHERE post.createdAt <= endOfYear
            WITH yearNum, count(post) AS count
            RETURN yearNum AS key, count
            ORDER BY key ASC
            """)
    List<CountDataProjection> getAllTimeYearlyStatistics();
}
