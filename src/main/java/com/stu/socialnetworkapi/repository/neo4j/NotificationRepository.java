package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.NotificationProjection;
import com.stu.socialnetworkapi.entity.Notification;
import com.stu.socialnetworkapi.enums.NotificationAction;
import com.stu.socialnetworkapi.enums.ObjectType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends Neo4jRepository<Notification, UUID> {
    @Query("""
                // Lấy người dùng nhận thông báo
                MATCH (receiver:User {id: $userId})
            
                // Lấy notification gửi tới user
                MATCH (receiver)-[:HAS_NOTIFICATION]->(n:Notification)-[:BY_USER]->(creator:User)
            
                // Lấy ảnh đại diện nếu có
                OPTIONAL MATCH (creator)-[:HAS_PROFILE_PICTURE]->(pf:File)
            
                // Xử lý theo targetType
                // Nếu targetType là POST, lấy post
                OPTIONAL MATCH (post:Post {id: n.targetId})
                WHERE n.targetType = 'POST'
            
                // Nếu targetType là COMMENT, lấy comment
                OPTIONAL MATCH (comment:Comment {id: n.targetId})
                WHERE n.targetType = 'COMMENT'
            
                // Nếu comment là replied comment, lấy comment cha
                OPTIONAL MATCH (comment)-[:REPLIED]->(originalComment:Comment)
            
                // Lấy post của comment
                // Nếu là replied comment, lấy post từ comment cha
                // Nếu là comment gốc, lấy post trực tiếp
                OPTIONAL MATCH (postFromComment:Post)-[:HAS_COMMENT]-(commentWithPost:Comment)
                WHERE commentWithPost = CASE
                    WHEN originalComment IS NOT NULL THEN originalComment
                    ELSE comment
                END
            
                // Gom kết quả
                WITH n, creator, pf, post, comment, originalComment, postFromComment
                ORDER BY n.sentAt DESC
                SKIP $skip LIMIT $limit
            
                // Trả về projection
                SET n.isRead = true
                RETURN n.id AS id,
                       n.action AS action,
                       n.targetType AS targetType,
                       n.targetId AS targetId,
                       CASE
                           WHEN n.targetType = 'POST' THEN post.id
                           WHEN n.targetType = 'COMMENT' THEN postFromComment.id
                           ELSE NULL
                       END AS postId,
                       CASE
                           WHEN n.targetType = 'COMMENT' AND originalComment IS NOT NULL THEN originalComment.id
                           WHEN n.targetType = 'COMMENT' AND originalComment IS NULL THEN comment.id
                           ELSE NULL
                       END AS commentId,
                       CASE
                           WHEN n.targetType = 'COMMENT' AND originalComment IS NOT NULL THEN comment.id
                           ELSE NULL
                       END AS repliedCommentId,
                       n.sentAt AS sentAt,
                       true as isRead,
                       creator.id AS userId,
                       creator.username AS username,
                       creator.givenName AS givenName,
                       creator.familyName AS familyName,
                       CASE WHEN pf IS NOT NULL THEN pf.id ELSE NULL END AS profilePictureId
            """)
    List<NotificationProjection> getNotifications(UUID userId, long skip, long limit);

    @Query("""
            MATCH (n:Notification)
            WHERE n.sentAt < $cutoffDate
            DETACH DELETE n
            """)
    void deleteOldNotifications(ZonedDateTime cutoffDate);

    @Query("""
            MATCH (creator:User {id: $creatorId})<-[:BY_USER]-(n:Notification)<-[:HAS_NOTIFICATION]-(receiver:User {id: $receiverId})
            WHERE n.action = $action
            AND n.targetId = $targetId
            AND n.targetType = $targetType
            RETURN n.id
            LIMIT 1
            """)
    Optional<UUID> findExistingNotification(
            UUID creatorId,
            UUID receiverId,
            NotificationAction action,
            UUID targetId,
            ObjectType targetType
    );

    @Query("""
            MATCH (receiver:User {id: $userId})-[:HAS_NOTIFICATION]->(n:Notification)
            WHERE n.isRead = false OR n.isRead IS NULL
            RETURN count(n) AS unreadCount
            """)
    long getUnreadNotificationCount(UUID userId);

    @Query("""
            MATCH (receiver:User {id: $userId})-[:HAS_NOTIFICATION]->(n:Notification)
            WHERE n.isRead = false OR n.isRead IS NULL
            WITH n
            ORDER BY n.sentAt DESC
            LIMIT $limit
            SET n.isRead = true
            """)
    void markLatestNotificationsAsRead(UUID userId, long limit);
}