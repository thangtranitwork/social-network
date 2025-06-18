package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.CountDataProjection;
import com.stu.socialnetworkapi.dto.projection.PostProjection;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.entity.Post;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends Neo4jRepository<Post, UUID> {
    @Query("""
            MATCH (author:User {id: $authorId})-[:POSTED]->(post:Post)
            OPTIONAL MATCH (viewer:User {id: $viewerId})-[friendship:FRIEND]->(author)
            WHERE post.deletedAt IS NULL
            AND (post.privacy = 'PUBLIC'
                OR (post.privacy = 'FRIEND' AND
                   ($viewerId = $authorId OR friendship IS NOT NULL))
                OR (post.privacy = 'PRIVATE' AND $viewerId = $authorId)
            )
            
            // Lấy thông tin bài viết gốc nếu là shared post
            OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
            OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
            OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
            OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
            
            // Kiểm tra block relationship giữa viewer và original author
            OPTIONAL MATCH (viewer:User {id: $viewerId})-[block:BLOCK]-(originalAuthor)
            
            // Kiểm tra friendship với original author
            OPTIONAL MATCH (viewer)-[originalFriendship:FRIEND]->(originalAuthor)
            
            // Lấy thông tin post hiện tại
            OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
            OPTIONAL MATCH (viewer)-[liked:LIKED]->(post)
            OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            
            WITH post, author, viewer, friendship, originalPost, originalAuthor, originalProfilePic, block, originalFriendship,
                 profilePic, liked,
                 COLLECT(DISTINCT file.id) AS files,
                 COLLECT(DISTINCT originalFile.id) AS originalFiles,
                 CASE
                   WHEN originalPost IS NULL THEN true  // Không phải shared post
                   WHEN originalPost.deletedAt IS NOT NULL THEN false  // Bài gốc đã bị xóa
                   WHEN block IS NOT NULL THEN false  // Có block relationship
                   WHEN originalPost.privacy = 'PUBLIC' THEN true
                   WHEN originalPost.privacy = 'FRIEND' AND
                        ($viewerId = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                   WHEN originalPost.privacy = 'PRIVATE' AND $viewerId = originalAuthor.id THEN true
                   ELSE false
                 END AS originalPostCanView
            
            RETURN post.id AS id,
                   post.content AS content,
                   post.createdAt AS createdAt,
                   post.updatedAt AS updatedAt,
                   post.privacy AS privacy,
                   files AS files,
                   post.likeCount AS likeCount,
                   post.shareCount AS shareCount,
                   post.commentCount AS commentCount,
                   liked IS NOT NULL AS liked,
                   author.id AS authorId,
                   author.username AS authorUsername,
                   author.givenName AS authorGivenName,
                   author.familyName AS authorFamilyName,
                   profilePic.id AS authorProfilePictureId,
                   friendship IS NOT NULL AS isFriend,
            
                   // Original post information
                   CASE WHEN originalPostCanView THEN originalPost.id ELSE null END AS originalPostId,
                   CASE WHEN originalPostCanView THEN originalPost.content ELSE null END AS originalPostContent,
                   CASE WHEN originalPostCanView THEN originalPost.createdAt ELSE null END AS originalPostCreatedAt,
                   CASE WHEN originalPostCanView THEN originalPost.updatedAt ELSE null END AS originalPostUpdatedAt,
                   CASE WHEN originalPostCanView THEN originalPost.privacy ELSE null END AS originalPostPrivacy,
                   CASE WHEN originalPostCanView THEN originalFiles ELSE [] END AS originalPostFiles,
                   CASE WHEN originalPostCanView THEN originalAuthor.id ELSE null END AS originalPostAuthorId,
                   CASE WHEN originalPostCanView THEN originalAuthor.username ELSE null END AS originalPostAuthorUsername,
                   CASE WHEN originalPostCanView THEN originalAuthor.givenName ELSE null END AS originalPostAuthorGivenName,
                   CASE WHEN originalPostCanView THEN originalAuthor.familyName ELSE null END AS originalPostAuthorFamilyName,
                   CASE WHEN originalPostCanView THEN originalProfilePic.id ELSE null END AS originalPostAuthorProfilePictureId,
                   originalPostCanView AS originalPostCanView
            
                   ORDER BY createdAt DESC
                   SKIP $skip LIMIT $limit
            """)
    List<PostProjection> findAllByAuthorId(UUID authorId, UUID viewerId, long skip, long limit);

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
                  AND post.deletedAt IS NULL
            
                OPTIONAL MATCH (me)-[liked:LIKED]->(post)
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
            
                OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
                OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
                OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
                OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
                OPTIONAL MATCH (me)-[:BLOCK]-(originalAuthor)
                OPTIONAL MATCH (me)-[originalFriendship:FRIEND]-(originalAuthor)
            
                // Check if user can view original post
                WITH post, author, me, liked, profilePic, file, originalPost, originalAuthor,
                     originalProfilePic, originalFile, score,
                     CASE
                         WHEN originalPost IS NULL THEN true
                         WHEN originalPost.deletedAt IS NOT NULL THEN false
                         WHEN (me)-[:BLOCK]-(originalAuthor) THEN false
                         WHEN originalPost.privacy = 'PUBLIC' THEN true
                         WHEN originalPost.privacy = 'FRIEND' AND
                              ($userId = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                         WHEN originalPost.privacy = 'PRIVATE' AND $userId = originalAuthor.id THEN true
                         ELSE false
                     END AS originalPostCanView
            
                WITH post, author, liked, profilePic, score, COLLECT(DISTINCT file.id) AS files,
                     originalPost, originalAuthor, originalProfilePic, COLLECT(DISTINCT originalFile.id) AS originalFiles,
                     originalPostCanView,
                     EXISTS((me)-[:FRIEND]-(author)) AS isFriend
            
                RETURN post.id AS id,
                       post.content AS content,
                       post.createdAt AS createdAt,
                       post.updatedAt AS updatedAt,
                       post.privacy AS privacy,
                       files AS files,
                       post.likeCount AS likeCount,
                       post.shareCount AS shareCount,
                       post.commentCount AS commentCount,
                       liked IS NOT NULL AS liked,
                       author.id AS authorId,
                       author.username AS authorUsername,
                       author.givenName AS authorGivenName,
                       author.familyName AS authorFamilyName,
                       profilePic.id AS authorProfilePictureId,
                       isFriend AS isFriend,
            
                       CASE WHEN originalPostCanView THEN originalPost.id ELSE null END AS originalPostId,
                       CASE WHEN originalPostCanView THEN originalPost.content ELSE null END AS originalPostContent,
                       CASE WHEN originalPostCanView THEN originalPost.createdAt ELSE null END AS originalPostCreatedAt,
                       CASE WHEN originalPostCanView THEN originalPost.updatedAt ELSE null END AS originalPostUpdatedAt,
                       CASE WHEN originalPostCanView THEN originalPost.privacy ELSE null END AS originalPostPrivacy,
                       CASE WHEN originalPostCanView THEN originalFiles ELSE [] END AS originalPostFiles,
                       CASE WHEN originalPostCanView THEN originalAuthor.id ELSE null END AS originalPostAuthorId,
                       CASE WHEN originalPostCanView THEN originalAuthor.username ELSE null END AS originalPostAuthorUsername,
                       CASE WHEN originalPostCanView THEN originalAuthor.givenName ELSE null END AS originalPostAuthorGivenName,
                       CASE WHEN originalPostCanView THEN originalAuthor.familyName ELSE null END AS originalPostAuthorFamilyName,
                       CASE WHEN originalPostCanView THEN originalProfilePic.id ELSE null END AS originalPostAuthorProfilePictureId,
                       originalPostCanView AS originalPostCanView
            
                ORDER BY score DESC, post.createdAt DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<PostProjection> fullTextSearch(String query, UUID userId, long limit, long skip);

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
                    AND post.deletedAt IS NULL
                    AND NOT (u)-[:BLOCK]-(author)
            
                    OPTIONAL MATCH p = shortestPath((u)-[*1..4]->(post))
            
                    OPTIONAL MATCH (u)-[vu:VIEW_PROFILE]->(author)
                    OPTIONAL MATCH (author)-[uv:VIEW_PROFILE]->(u)
            
                    // Lấy thông tin bài viết gốc nếu là shared post
                    OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
                    OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
                    OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
                    OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
            
                    // Kiểm tra block relationship giữa viewer và original author
                    OPTIONAL MATCH (u)-[block:BLOCK]-(originalAuthor)
            
                    // Kiểm tra friendship với original author
                    OPTIONAL MATCH (u)-[originalFriendship:FRIEND]->(originalAuthor)
            
                    // Lấy thông tin post hiện tại
                    OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
                    OPTIONAL MATCH (u)-[liked:LIKED]->(post)
                    OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            
                    WITH u, post, author,
                         CASE WHEN p IS NULL THEN NULL ELSE length(p) END AS shortestPathLength,
                         coalesce(vu.times, 0) AS viewForward,
                         coalesce(uv.times, 0) AS viewBackward,
                         originalPost, originalAuthor, originalProfilePic, block, originalFriendship,
                         profilePic, liked,
                         COLLECT(DISTINCT file.id) AS files,
                         COLLECT(DISTINCT originalFile.id) AS originalFiles,
            
                         // Kiểm tra xem bài viết gốc có thể xem được không
                         CASE
                             WHEN originalPost IS NULL THEN true
                             WHEN originalPost.deletedAt IS NOT NULL THEN false
                             WHEN block IS NOT NULL THEN false
                             WHEN originalPost.privacy = 'PUBLIC' THEN true
                             WHEN originalPost.privacy = 'FRIEND' AND
                                  ($userId = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                             WHEN originalPost.privacy = 'PRIVATE' AND $userId = originalAuthor.id THEN true
                             ELSE false
                         END AS originalPostCanView,
            
                         // Tính điểm
                         CASE WHEN post.createdAt > datetime() - duration('P1D') THEN 200 ELSE 0 END AS newPostScore,
                         post.likeCount * 2 AS likeScore,
                         post.commentCount * 3 AS commentScore,
                         post.shareCount * 5 AS shareScore
            
                    WITH post, author, u, files, originalPost, originalAuthor, originalProfilePic, originalFiles, liked, profilePic,
                         originalPostCanView, shortestPathLength, viewForward, viewBackward, newPostScore, likeScore, commentScore, shareScore,
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
                         END AS pathScore
            
                    WITH post, author, u, files, originalPost, originalAuthor, originalProfilePic, originalFiles, liked, profilePic,
                         originalPostCanView,
                         pathScore + newPostScore + relationshipScore + likeScore + commentScore + shareScore AS totalScore,
                         EXISTS((u)-[:FRIEND]-(author)) AS isFriend
            
                    ORDER BY totalScore DESC, post.createdAt DESC
            
                    RETURN post.id AS id,
                           post.content AS content,
                           post.createdAt AS createdAt,
                           post.updatedAt AS updatedAt,
                           post.privacy AS privacy,
                           files AS files,
                           post.likeCount AS likeCount,
                           post.shareCount AS shareCount,
                           post.commentCount AS commentCount,
                           liked IS NOT NULL AS liked,
                           author.id AS authorId,
                           author.username AS authorUsername,
                           author.givenName AS authorGivenName,
                           author.familyName AS authorFamilyName,
                           profilePic.id AS authorProfilePictureId,
                           isFriend AS isFriend,
            
                           // Original post information
                           CASE WHEN originalPostCanView THEN originalPost.id ELSE null END AS originalPostId,
                           CASE WHEN originalPostCanView THEN originalPost.content ELSE null END AS originalPostContent,
                           CASE WHEN originalPostCanView THEN originalPost.createdAt ELSE null END AS originalPostCreatedAt,
                           CASE WHEN originalPostCanView THEN originalPost.updatedAt ELSE null END AS originalPostUpdatedAt,
                           CASE WHEN originalPostCanView THEN originalPost.privacy ELSE null END AS originalPostPrivacy,
                           CASE WHEN originalPostCanView THEN originalFiles ELSE [] END AS originalPostFiles,
                           CASE WHEN originalPostCanView THEN originalAuthor.id ELSE null END AS originalPostAuthorId,
                           CASE WHEN originalPostCanView THEN originalAuthor.username ELSE null END AS originalPostAuthorUsername,
                           CASE WHEN originalPostCanView THEN originalAuthor.givenName ELSE null END AS originalPostAuthorGivenName,
                           CASE WHEN originalPostCanView THEN originalAuthor.familyName ELSE null END AS originalPostAuthorFamilyName,
                           CASE WHEN originalPostCanView THEN originalProfilePic.id ELSE null END AS originalPostAuthorProfilePictureId,
                           originalPostCanView AS originalPostCanView
            
                    SKIP $skip
                    LIMIT $limit
            """)
    List<PostProjection> getSuggestedPosts(UUID userId, long skip, long limit);

    @Query("""
            WITH datetime() AS today
            WITH today,
                 datetime({year: today.year, month: today.month, day: 1}) AS startOfMonth,
                 datetime({year: today.year, month: 1, day: 1}) AS startOfYear,
                 datetime($startOfWeek) AS startOfWeek
            
            MATCH (post:Post)
            OPTIONAL MATCH (post)-[attach:ATTACH_FILES]->(:File)
            WHERE post.deleteAt IS NOT NULL
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
