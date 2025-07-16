package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.CountDataProjection;
import com.stu.socialnetworkapi.dto.projection.PostProjection;
import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.entity.Post;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends Neo4jRepository<Post, UUID> {
    @Query("""
                MATCH (post:Post {id: $id})
                WHERE post.deleteAt IS NULL
                MATCH (author:User)-[:POSTED]->(post)
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
                OPTIONAL MATCH (viewer:User {username: $username})
                OPTIONAL MATCH (viewer)-[friendship:FRIEND]->(author)
                OPTIONAL MATCH (viewer)-[liked:LIKED]->(post)
            
                OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
                OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
                OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
                OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
                OPTIONAL MATCH (viewer)-[originalFriendship:FRIEND]->(originalAuthor)
            
                WITH post, author, viewer, friendship, liked, profilePic,
                     originalPost, originalAuthor, originalProfilePic, originalFriendship,
                     COLLECT(DISTINCT file.id) AS files,
                     COLLECT(DISTINCT originalFile.id) AS originalFiles,
            
                     CASE
                       WHEN originalPost IS NULL THEN false
                       WHEN originalPost.deletedAt IS NOT NULL THEN false
                       WHEN originalPost.privacy = 'PUBLIC' THEN true
                       WHEN originalPost.privacy = 'FRIEND' AND
                            (viewer.username = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                       WHEN originalPost.privacy = 'PRIVATE' AND viewer.username = originalAuthor.id THEN true
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
                       originalPost IS NOT NULL AS isSharedPost,
            
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
            """)
    Optional<PostProjection> findPostProjectionById(UUID id, String viewerUsername);

    @Query("""
            // Match users và posts trước
            MATCH (author:User {username: $authorUsername}), (viewer:User {username: $viewerUsername})
            MATCH (author)-[:POSTED]->(post:Post)
            WHERE post.deletedAt IS NULL
            
            // Kiểm tra friendship riêng biệt
            OPTIONAL MATCH (viewer)-[friendship:FRIEND]->(author)
            
            // Kiểm tra privacy
            WHERE (
                $viewerUsername = $authorUsername
                OR post.privacy = 'PUBLIC'
                OR (post.privacy = 'FRIEND' AND friendship IS NOT NULL)
            )
            
            // Lấy thông tin bài viết gốc nếu là shared post
            OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
            OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
            OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
            OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
            
            // Kiểm tra block relationship giữa viewer và original author
            OPTIONAL MATCH (viewer)-[block:BLOCK]-(originalAuthor)
            
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
                        ($viewerUsername = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                   WHEN originalPost.privacy = 'PRIVATE' AND $viewerUsername = originalAuthor.id THEN true
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
                   originalPost IS NOT NULL AS isSharedPost,
            
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
            
                   ORDER BY post.createdAt DESC
                   SKIP $skip LIMIT $limit
            """)
    List<PostProjection> findAllByAuthorUsername(String authorUsername, String viewerUsername, long skip, long limit);

    @Query("""
            MATCH (p:Post {id: $postId})<-[like:LIKED]-(u:User {id: $likerId})
            RETURN COUNT(like) > 0
            """)
    boolean isLiked(UUID postId, UUID likerId);

    @Query("""
                // Match user trước
                MATCH (me:User {id: $userId})
                CALL db.index.fulltext.queryNodes("postSearchIndex", $query + "*")
                YIELD node AS post, score
            
                MATCH (author:User)-[:POSTED]->(post)
                WHERE NOT (author)-[:BLOCKED]->(me)
                  AND NOT (me)-[:BLOCKED]->(author)
                  AND post.deletedAt IS NULL
            
                // Kiểm tra friendship riêng biệt
                OPTIONAL MATCH (me)-[friendship:FRIEND]->(author)
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
                     originalProfilePic, originalFile, score, friendship,
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
                     originalPostCanView, friendship IS NOT NULL AS isFriend
            
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
                       originalPost IS NOT NULL AS isSharedPost,
            
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
     * Truy vấn đề xuất bài viết dựa trên hệ thống tính điểm, bao gồm:
     * Hệ thống tính điểm:
     * - Bài viết mới trong 24h: 240 - 10 * số giờ đã trôi qua
     * - Bạn bè trực tiếp: +100 điểm
     * - Bạn của bạn hoặc đã gửi yêu cầu kết bạn: +50 điểm
     * - Độ dài đường đi ngắn nhất từ user đến post ≥ 2: + (120 / độ dài)
     * - Xem trang cá nhân của tác giả: +2 điểm * số lần xem
     * - Tác giả xem trang cá nhân của user: +1 điểm * số lần xem
     * - Số like: +2 điểm * likeCount
     * - Số comment: +3 điểm * commentCount
     * - Số share: +5 điểm * shareCount
     * - Điểm tương tác với từ khóa (keywordScore): tổng `INTERACT_WITH` giữa user và các keyword của bài viết
     * - Bài viết đã được tải (loaded.times): -20 điểm * số lần đã load
     * Chỉ trả về bài viết:
     * - Không bị xóa (deletedAt IS NULL)
     * - Không bị block giữa user và tác giả
     * - Thỏa mãn điều kiện quyền riêng tư:
     *    + PUBLIC
     *    + FRIEND nếu là bạn
     * - Nếu là bài viết chia sẻ:
     *    + Kiểm tra quyền truy cập bài viết gốc
     * Truy vấn trả về:
     * - Thông tin bài viết, tác giả, và bài viết gốc nếu có
     * - Tổng điểm (score) để phục vụ sắp xếp
     * Đồng thời cập nhật số lần đã xem bài viết (LOADED.times) giữa user và bài viết.
     */

    @Query("""
                    // Match user trước
                    MATCH (u:User {id: $userId})
                    MATCH (author:User)-[:POSTED]->(post:Post)
                    WHERE post.deletedAt IS NULL
                    AND NOT (u)-[:BLOCK]-(author)
                    AND (
                        post.privacy = 'PUBLIC'
                        OR (post.privacy = 'FRIEND' AND EXISTS((u)-[:FRIEND]->(author)))
                        )
                    OPTIONAL MATCH (u)-[friendship:FRIEND]->(author)
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
                    OPTIONAL MATCH (post)-[:HAS_KEYWORDS]->(keyword:Keyword)
                    OPTIONAL MATCH (u)-[inter:INTERACT_WITH]->(keyword)
                    OPTIONAL MATCH (u)-[liked:LIKED]->(post)
                    OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                    OPTIONAL MATCH (u)-[loaded:LOADED]->(post)
            
                    WITH u, post, author, friendship, loaded,
                         CASE WHEN p IS NULL THEN NULL ELSE length(p) END AS shortestPathLength,
                         coalesce(vu.times, 0) AS viewForward,
                         coalesce(uv.times, 0) AS viewBackward,
                         originalPost, originalAuthor, originalProfilePic, block, originalFriendship,
                         profilePic, liked,
                         COLLECT(DISTINCT file.id) AS files,
                         COLLECT(DISTINCT originalFile.id) AS originalFiles,
                         COALESCE(SUM(inter.score), 0) AS keywordScore,

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
                        CASE
                            WHEN post.createdAt > datetime() - duration('P1D')
                            THEN 240 - duration.between(post.createdAt, datetime()).hours * 10
                            ELSE 0
                            END AS newPostScore,
                         post.likeCount * 2 AS likeScore,
                         post.commentCount * 3 AS commentScore,
                         post.shareCount * 5 AS shareScore,
            
                         CASE
                            WHEN loaded IS NOT NULL THEN loaded.times * (-20)
                            ELSE 0
                         END AS loadedScore
            
            
                    WITH post, author, u, files, originalPost, originalAuthor, originalProfilePic, originalFiles, liked, profilePic,
                         originalPostCanView, shortestPathLength, viewForward, viewBackward, newPostScore, likeScore, commentScore, shareScore, friendship, loadedScore, keywordScore,
                         CASE
                             WHEN friendship IS NOT NULL THEN 100
                             WHEN (u)-[:FRIEND]-()-[:FRIEND]-(author) AND friendship IS NULL OR (u)-[:REQUEST]-(author) THEN 50
                             ELSE 0
                         END
                         + 2 * viewForward
                         + 1 * viewBackward AS relationshipScore,
                         CASE
                             WHEN shortestPathLength IS NULL OR shortestPathLength = 1 THEN 0
                             ELSE 120.0 / shortestPathLength
                         END AS pathScore
            
                    WITH post, author, u, files, originalPost, originalAuthor, originalProfilePic, originalFiles, liked, profilePic, loadedScore,
                         originalPostCanView, friendship, keywordScore,
                         pathScore + newPostScore + relationshipScore + likeScore + commentScore + shareScore + loadedScore + keywordScore AS totalScore
            
                    ORDER BY totalScore DESC, post.createdAt DESC
                    SKIP $skip
                    LIMIT $limit
            
                    WITH post, author, u, files, originalPost, originalAuthor, originalProfilePic, originalFiles, liked, profilePic,
                        originalPostCanView, friendship, totalScore
                    MERGE (u)-[loaded:LOADED]->(post)
                    ON CREATE SET loaded.times = 1
                    ON MATCH SET loaded.times = loaded.times + 1
            
                    RETURN post.id AS id,
                           totalScore AS score,
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
                           originalPost IS NOT NULL AS isSharedPost,
            
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
            """)
    List<PostProjection> getSuggestedPosts(UUID userId, long skip, long limit);

    @Query("""
                // Match user trước
                MATCH (u:User {id: $userId})
                MATCH (u)-[friendship:FRIEND]->(friend:User)
                MATCH (friend)-[:POSTED]->(post:Post)
                WHERE post.deletedAt IS NULL
                  AND (post.privacy = 'FRIEND' OR post.privacy = 'PUBLIC')
                  AND NOT (u)-[:BLOCK]-(friend)
            
                // Kiểm tra friendship riêng biệt
                OPTIONAL MATCH (u)-[liked:LIKED]->(post)
                OPTIONAL MATCH (friend)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
            
                // Xử lý nếu là bài chia sẻ
                OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
                OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
                OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
                OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)
            
                // Check block
                OPTIONAL MATCH (u)-[:BLOCK]-(originalAuthor)
                OPTIONAL MATCH (u)-[originalFriendship:FRIEND]-(originalAuthor)
            
                WITH post, friend AS author, u, liked, profilePic, COLLECT(DISTINCT file.id) AS files,
                     originalPost, originalAuthor, originalProfilePic, COLLECT(DISTINCT originalFile.id) AS originalFiles,
            
                     CASE
                         WHEN originalPost IS NULL THEN true
                         WHEN originalPost.deletedAt IS NOT NULL THEN false
                         WHEN (u)-[:BLOCK]-(originalAuthor) THEN false
                         WHEN originalPost.privacy = 'PUBLIC' THEN true
                         WHEN originalPost.privacy = 'FRIEND' AND
                              (u.id = originalAuthor.id OR originalFriendship IS NOT NULL) THEN true
                         WHEN originalPost.privacy = 'PRIVATE' AND u.id = originalAuthor.id THEN true
                         ELSE false
                     END AS originalPostCanView
            
                ORDER BY post.createdAt DESC
                SKIP $skip
                LIMIT $limit
            
                WITH post, author, liked, profilePic, files, originalPost, originalAuthor, originalProfilePic, originalFiles, originalPostCanView
                MERGE (u)-[loaded:LOADED]->(post)
                ON CREATE SET loaded.times = 1
                ON MATCH SET loaded.times = loaded.times + 1
            
                RETURN
                    post.id AS id,
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
                    true AS isFriend,
                    originalPost IS NOT NULL AS isSharedPost,
            
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
            """)
    List<PostProjection> getFriendPostsOnly(UUID userId, long skip, long limit);

    @Query("""
            // Match user trước
            MATCH (u:User {id: $userId})
            MATCH (author:User)-[:POSTED]->(post:Post)
            WHERE post.deletedAt IS NULL
              AND NOT (u)-[:BLOCK]-(author)

            // Kiểm tra privacy với friendship riêng biệt
            OPTIONAL MATCH (u)-[friendship:FRIEND]->(author)
            WHERE (
                post.privacy = 'PUBLIC' OR
                (post.privacy = 'FRIEND' AND friendship IS NOT NULL)
            )

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
            OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            OPTIONAL MATCH (u)-[liked:LIKED]->(post)

            WITH post, author, u, liked, profilePic, friendship,
                 COLLECT(DISTINCT file.id) AS files,
                 originalPost, originalAuthor, originalProfilePic,
                 COLLECT(DISTINCT originalFile.id) AS originalFiles,
                 block, originalFriendship,

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
                 END AS originalPostCanView

            ORDER BY post.createdAt DESC
            SKIP $skip
            LIMIT $limit

            WITH post, author, u, liked, profilePic, friendship, files,
                 originalPost, originalAuthor, originalProfilePic, originalFiles, originalPostCanView

            MERGE (u)-[loaded:LOADED]->(post)
            ON CREATE SET loaded.times = 1
            ON MATCH SET loaded.times = loaded.times + 1

            RETURN
                post.id AS id,
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
                originalPost IS NOT NULL AS isSharedPost,

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
        """)
    List<PostProjection> getPostsOrderByCreatedAtDesc(UUID userId, long skip, long limit);

    @Query("""
            MATCH (author:User)-[:POSTED]->(post:Post)
            WHERE post.deletedAt IS NULL
            WITH post
            ORDER BY post.created DESC
            SKIP $skip LIMIT $limit

            // Lấy thông tin bài viết gốc nếu là shared post
            OPTIONAL MATCH (post)-[:SHARED]->(originalPost:Post)
            OPTIONAL MATCH (originalPost)<-[:POSTED]-(originalAuthor:User)
            OPTIONAL MATCH (originalAuthor)-[:HAS_PROFILE_PICTURE]->(originalProfilePic:File)
            OPTIONAL MATCH (originalPost)-[:ATTACH_FILES]->(originalFile:File)

            // Lấy thông tin post hiện tại
            OPTIONAL MATCH (post)-[:ATTACH_FILES]->(file:File)
            OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)

            WITH post, author, profilePic,
                 COLLECT(DISTINCT file.id) AS files,
                 originalPost, originalAuthor, originalProfilePic,
                 COLLECT(DISTINCT originalFile.id) AS originalFiles,

                 // Kiểm tra xem bài viết gốc có thể xem được không
                 CASE
                   WHEN originalPost IS NULL THEN true
                   WHEN originalPost.deletedAt IS NOT NULL THEN false
                   else true
                 END AS originalPostCanView

            WITH post, author, profilePic, files,
                 originalPost, originalAuthor, originalProfilePic, originalFiles, originalPostCanView

            RETURN
                post.id AS id,
                post.content AS content,
                post.createdAt AS createdAt,
                post.updatedAt AS updatedAt,
                post.privacy AS privacy,
                files AS files,
                post.likeCount AS likeCount,
                post.shareCount AS shareCount,
                post.commentCount AS commentCount,
                author.id AS authorId,
                author.username AS authorUsername,
                author.givenName AS authorGivenName,
                author.familyName AS authorFamilyName,
                profilePic.id AS authorProfilePictureId,
                originalPost IS NOT NULL AS isSharedPost,

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
        """)    List<PostProjection> getAllOrderByCreatedAtDesc(long skip, long limit);

    @Query("""
            WITH datetime() AS today
            WITH today,
                 datetime({year: today.year, month: today.month, day: today.day, hour: 0, minute: 0, second: 0}) AS startOfDay,
                 datetime({year: today.year, month: today.month, day: 1}) AS startOfMonth,
                 datetime({year: today.year, month: 1, day: 1}) AS startOfYear,
                 datetime($startOfWeek) AS startOfWeek
            
            MATCH (post:Post)
            WHERE post.deleteAt IS NULL
            OPTIONAL MATCH (post)-[attach:ATTACH_FILES]->(:File)
            RETURN
              count(post) AS totalPosts,
              sum(post.likeCount) AS totalLikes,
              sum(post.commentCount) AS totalComments,
              sum(post.shareCount) AS totalShares,
              count(attach) AS totalFiles,
              count(CASE WHEN post.createdAt >= startOfDay THEN 1 END) AS newPostsToday,
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