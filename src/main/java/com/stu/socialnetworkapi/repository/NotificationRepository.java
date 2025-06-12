package com.stu.socialnetworkapi.repository;

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
            
                // Gom kết quả
                WITH n, creator, pf
                ORDER BY n.sentAt DESC
                SKIP $skip LIMIT $limit
                // Trả về projection
                SET n.isRead = true
                RETURN n.id AS id,
                       n.action AS action,
                       n.targetType AS targetType,
                       n.targetId AS targetId,
                       n.sentAt AS sentAt,
                       n.isRead as isRead,
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
}
