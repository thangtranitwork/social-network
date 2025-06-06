package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Post;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

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
    Hệ thống chấm điểm
    - Bài viết mới 24 giờ: 200 điểm
    - Bài viết của bạn user: 100 điểm
    - Bài viết của bạn của bạn của user: 50 điểm
    - Số like: 2 điểm
    - Số comment: 3 điểm
    - Số share: 5 điểm
    Cần kiểm tra privacy
    */

    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (author:User)-[:POSTED]->(post:Post)
            
            // Điều kiện xem được bài viết
            WHERE (
                post.privacy = 'PUBLIC'
                OR (post.privacy = 'FRIEND' AND (u)-[:FRIEND]-(author))
            )
            // Loại bỏ bài viết của tác giả đã chặn hoặc bị chặn
            AND NOT (u)-[:BLOCK]-(author)
            
            // Tính điểm theo từng tiêu chí
            WITH post, author,
                 // Bài viết mới 24 giờ: 200 điểm
                 CASE WHEN post.createdAt > datetime() - duration('P1D') THEN 200 ELSE 0 END AS newPostScore,
            
                 // Điểm theo mối quan hệ với tác giả
                 CASE
                     WHEN (u)-[:FRIEND]-(author) THEN 100  // Bài viết của bạn: 100 điểm
                     WHEN (u)-[:FRIEND]-()-[:FRIEND]-(author) AND NOT (u)-[:FRIEND]-(author) THEN 50  // Bài viết của bạn của bạn: 50 điểm
                     ELSE 0
                 END AS relationshipScore,
            
                 // Điểm tương tác
                 post.likeCount * 2 AS likeScore,      // Số like: 2 điểm
                 post.commentCount * 3 AS commentScore, // Số comment: 3 điểm
                 post.shareCount * 5 AS shareScore     // Số share: 5 điểm
            
            WITH post,
                 newPostScore + relationshipScore + likeScore + commentScore + shareScore AS totalScore
            
            ORDER BY totalScore DESC, post.createdAt DESC
            
            RETURN post.id AS id
            SKIP $skip
            LIMIT $limit
            """)
    List<UUID> getSuggestedPosts(UUID userId, Pageable pageable);

}
