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

    /*
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
            
            // Bài viết của bạn (FRIEND)
            OPTIONAL MATCH (u)-[:FRIEND]-(f:User)-[:POSTED]->(p1:Post)
            WHERE p1.privacy IN ['PUBLIC', 'FRIENDS_ONLY']
              AND NOT (u)-[:BLOCKED]->(f)
              AND NOT (f)-[:BLOCKED]->(u)
            WITH u, collect({post: p1, author: f}) AS friendPosts
            
            // Bài viết của bạn của bạn (FOAF)
            OPTIONAL MATCH (u)-[:FRIEND]-(f1:User)-[:FRIEND]-(f2:User)-[:POSTED]->(p2:Post)
            WHERE NOT (u)-[:FRIEND]-(f2)
              AND p2.privacy = 'PUBLIC'
              AND NOT (u)-[:BLOCKED]->(f2)
              AND NOT (f2)-[:BLOCKED]->(u)
            WITH u, friendPosts + collect({post: p2, author: f2}) AS foafPosts
            
            // Bài viết công khai của người lạ
            OPTIONAL MATCH (stranger:User)-[:POSTED]->(p3:Post)
            WHERE p3.privacy = 'PUBLIC'
              AND NOT (u)-[:FRIEND]-(stranger)
              AND NOT (u)-[:FRIEND]-()-[:FRIEND]-(stranger)
              AND NOT (u)-[:BLOCKED]->(stranger)
              AND NOT (stranger)-[:BLOCKED]->(u)
            WITH friendPosts + foafPosts + collect({post: p3, author: stranger}) AS allPosts
            
            UNWIND allPosts AS entry
            WITH entry.post AS post, entry.author AS author
            
            WITH post,
              CASE WHEN post.createdAt > datetime().minusDays(1) THEN 200 ELSE 0 END +
              CASE
                WHEN ( (author)-[:FRIEND]-(:User {id: $userId}) ) THEN 100
                WHEN ( (author)-[:FRIEND]-()-[:FRIEND]-(:User {id: $userId}) ) THEN 50
                ELSE 0
              END +
              post.likeCount * 2 +
              post.commentCount * 3 +
              post.shareCount * 5 AS score
            
            ORDER BY score DESC, post.createdAt DESC
            RETURN post.id AS id
            SKIP $skip
            LIMIT $limit
            """)
    List<UUID> getSuggestedPosts(UUID userId, Pageable pageable);

}
