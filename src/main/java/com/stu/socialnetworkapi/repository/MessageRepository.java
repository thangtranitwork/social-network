package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Message;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends Neo4jRepository<Message, UUID> {
    @Query("""
            MATCH (sender:User)-[sent:SENT]->(message:Message)<-[hm:HAS_MESSAGE]->(chat:Chat {id: $chatId})
            OPTIONAL MATCH (message)-[attach:ATTACH_FILE]->(attachedFile:File)
            RETURN message, sent, sender, attach, attachedFile, hm, chat
            ORDER BY message.sentAt DESC
            SKIP $skip LIMIT $limit
            """)
    List<Message> findAllByChatIdOrderBySentAtDesc(UUID chatId, long skip, long limit);

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