package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends Neo4jRepository<Message, UUID> {
    Slice<Message> findAllByChatId(UUID chatId, Pageable pageable);
}
