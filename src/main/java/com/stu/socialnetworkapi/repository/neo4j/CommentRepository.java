package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.CommentProjection;
import com.stu.socialnetworkapi.entity.Comment;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends Neo4jRepository<Comment, UUID> {
    @Query("""
            MATCH (c:Comment {id: $commentId})<-[like:LIKED]-(u:User {id: $likerId})
            RETURN COUNT(like) > 0
            """)
    boolean isLiked(UUID commentId, UUID likerId);

    @Query("""
            MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
            WHERE c.originalComment IS NULL
            MATCH (c)<-[:COMMENTED]-(author:User)
            MATCH (viewer:User {username: $username})
            MATCH (p)<-[:POSTED]-(postAuthor:User)
            
            // Filter out blocked users (both directions)
            WHERE NOT (viewer)-[:BLOCKED]-(author)
            
            OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
            OPTIONAL MATCH (c)-[:ATTACH_FILE]->(attachedFile:File)
            OPTIONAL MATCH (c)<-[:LIKED]-(viewer)
            
            // Calculate relationship distance with post author priority
            OPTIONAL MATCH relationshipPath = shortestPath((viewer)-[:FRIEND*0..2]-(author))
            WITH c, author, profilePic, attachedFile, postAuthor, relationshipPath,
                 COUNT(DISTINCT CASE WHEN (c)<-[:LIKED]-(viewer) THEN 1 END) > 0 AS liked,
                 CASE
                     WHEN relationshipPath IS NOT NULL AND length(relationshipPath) = 0 THEN 0  // Chính mình
                     WHEN author.id = postAuthor.id THEN 1                                      // Tác giả bài viết
                     WHEN relationshipPath IS NOT NULL AND length(relationshipPath) = 1 THEN 2  // Bạn trực tiếp
                     WHEN relationshipPath IS NOT NULL AND length(relationshipPath) = 2 THEN 3  // Bạn của bạn
                     ELSE 4                                                                     // Người lạ
                 END AS relationshipDistance,
                 CASE WHEN relationshipPath IS NOT NULL AND length(relationshipPath) = 1 THEN true ELSE false END AS isFriend
            
            RETURN
                c.id AS commentId,
                c.content AS content,
                c.likeCount AS likeCount,
                c.replyCount AS replyCount,
                attachedFile.id AS attachmentId,
                c.createdAt AS createdAt,
                c.updatedAt AS updatedAt,
                liked AS liked,
                author.id AS authorId,
                author.username AS authorUsername,
                author.givenName AS authorGivenName,
                author.familyName AS authorFamilyName,
                profilePic.id AS authorProfilePictureId,
                CASE WHEN $username = author.id THEN null ELSE isFriend END AS isFriend
            ORDER BY relationshipDistance ASC, c.createdAt DESC
            SKIP $skip
            LIMIT $limit
            """)
    List<CommentProjection> getSuggestedComments(UUID postId, String username, long skip, long limit);

    @Query("""
                MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
                WHERE c.originalComment IS NULL
                MATCH (c)<-[:COMMENTED]-(author:User)
                MATCH (viewer:User {username: $username})
            
                // Filter out blocked users (both directions)
                WHERE NOT (viewer)-[:BLOCKED]-(author)
            
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (c)-[:ATTACH_FILE]->(attachedFile:File)
                OPTIONAL MATCH (c)<-[:LIKED]-(viewer)
            
                WITH c, author, profilePic, attachedFile,
                     COUNT(DISTINCT CASE WHEN (c)<-[:LIKED]-(viewer) THEN 1 END) > 0 AS liked,
                     EXISTS((viewer)-[:FRIEND]-(author)) AS isFriend
            
                RETURN
                    c.id AS commentId,
                    c.content AS content,
                    c.likeCount AS likeCount,
                    c.replyCount AS replyCount,
                    attachedFile.id AS attachmentId,
                    c.createdAt AS createdAt,
                    c.updatedAt AS updatedAt,
                    liked AS liked,
                    author.id AS authorId,
                    author.username AS authorUsername,
                    author.givenName AS authorGivenName,
                    author.familyName AS authorFamilyName,
                    profilePic.id AS authorProfilePictureId,
                    CASE WHEN $username = author.username THEN null ELSE isFriend END AS isFriend
                ORDER BY c.createdAt DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<CommentProjection> getCommentsOrderByCreatedAtDesc(UUID postId, String username, long skip, long limit);

    @Query("""
                MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
                WHERE c.originalComment IS NULL
                MATCH (c)<-[:COMMENTED]-(author:User)
                MATCH (viewer:User {username: $username})
            
                WHERE (viewer)-[:FRIEND]-(author)
            
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (c)-[:ATTACH_FILE]->(attachedFile:File)
                OPTIONAL MATCH (c)<-[:LIKED]-(viewer)
            
                WITH c, author, profilePic, attachedFile,
                     COUNT(DISTINCT CASE WHEN (c)<-[:LIKED]-(viewer) THEN 1 END) > 0 AS liked
            
                RETURN
                    c.id AS commentId,
                    c.content AS content,
                    c.likeCount AS likeCount,
                    c.replyCount AS replyCount,
                    attachedFile.id AS attachmentId,
                    c.createdAt AS createdAt,
                    c.updatedAt AS updatedAt,
                    liked AS liked,
                    author.id AS authorId,
                    author.username AS authorUsername,
                    author.givenName AS authorGivenName,
                    author.familyName AS authorFamilyName,
                    profilePic.id AS authorProfilePictureId,
                    true AS isFriend
                ORDER BY c.createdAt DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<CommentProjection> getFriendComments(UUID postId, String username, long skip, long limit);


    @Query("""
                MATCH (originalComment:Comment {id: $originalCommentId})<-[:REPLIED]-(c:Comment)
                MATCH (c)<-[:COMMENTED]-(author:User)
                MATCH (viewer:User {id: $viewerId})
            
                // Filter out blocked users (both directions)
                WHERE NOT (viewer)-[:BLOCKED]-(author)
            
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(profilePic:File)
                OPTIONAL MATCH (c)-[:ATTACH_FILE]->(attachedFile:File)
                OPTIONAL MATCH (c)<-[:LIKED]-(viewer)
            
                WITH c, author, profilePic, attachedFile,
                     COUNT(DISTINCT CASE WHEN (c)<-[:LIKED]-(viewer) THEN 1 END) > 0 AS liked
            
                RETURN
                    c.id AS commentId,
                    c.content AS content,
                    c.likeCount AS likeCount,
                    c.replyCount AS replyCount,
                    attachedFile.id AS attachmentId,
                    c.createdAt AS createdAt,
                    c.updatedAt AS updatedAt,
                    liked AS liked,
                    author.id AS authorId,
                    author.username AS authorUsername,
                    author.givenName AS authorGivenName,
                    author.familyName AS authorFamilyName,
                    profilePic.id AS authorProfilePictureId,
                    CASE WHEN $viewerId = author.id THEN null ELSE null END AS isFriend
                ORDER BY c.createdAt DESC
                SKIP $skip
                LIMIT $limit
            """)
    List<CommentProjection> findRepliedCommentByOriginalCommentId(UUID originalCommentId, UUID viewerId, long skip, long limit);


}