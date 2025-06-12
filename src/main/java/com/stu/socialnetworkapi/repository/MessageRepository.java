package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Message;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends Neo4jRepository<Message, UUID> {
    List<Message> findAllByChatId(UUID chatId, Pageable pageable);

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