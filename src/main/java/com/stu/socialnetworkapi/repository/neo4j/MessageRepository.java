package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.dto.projection.MessageProjection;
import com.stu.socialnetworkapi.entity.Message;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends Neo4jRepository<Message, UUID> {
    @Query("""
            MATCH (sender:User)-[sent:SENT]->(message:Message)<-[hm:HAS_MESSAGE]-(chat:Chat {id: $chatId})
            OPTIONAL MATCH (message)-[attach:ATTACH_FILE]->(attachedFile:File)
            RETURN
                message.id AS id,
                chat.id AS chatId,
                message.content AS content,
                message.sentAt AS sentAt,
                message.isRead AS isRead,
                sender.id AS senderId,
                sender.username AS senderUsername,
                sender.givenName AS senderGivenName,
                sender.familyName AS senderFamilyName,
                sender.profilePictureId AS senderProfilePictureId,
                attachedFile.id AS attachmentId,
                attachedFile.name AS attachmentName,
                CASE WHEN message.deleteAt IS NOT NULL THEN true ELSE false END AS deleted,
                CASE WHEN message.updateAt IS NOT NULL THEN true ELSE false END AS updated,
                message.type AS type,
                CASE
                    WHEN message:Call THEN message.callId
                    ELSE null
                END AS callId,
                CASE
                    WHEN message:Call THEN message.callAt
                    ELSE null
                END AS callAt,
                CASE
                    WHEN message:Call THEN message.answerAt
                    ELSE null
                END AS answerAt,
                CASE
                    WHEN message:Call THEN message.endAt
                    ELSE null
                END AS endAt,
                CASE
                    WHEN message:Call THEN message.isAnswered
                    ELSE false
                END AS isAnswered,
                CASE
                    WHEN message:Call THEN message.isVideoCall
                    ELSE false
                END AS isVideoCall
            ORDER BY message.sentAt DESC
            SKIP $skip LIMIT $limit
            """)
    List<MessageProjection> findAllByChatIdOrderBySentAtDesc(UUID chatId, long skip, long limit);

    @Query("""
            MATCH (u:User)-[:IS_MEMBER_OF]->(c:Chat)-[:HAS_MESSAGE]->(m:Message)<-[:SENT]-(sender:User)
            WHERE c.id = $chatId
            AND u.id = $userId
            AND sender.id <> $userId
            AND m.isRead = false
            SET m.isRead = true
            """)
    void markAsRead(UUID chatId, UUID userId);
}