package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.entity.relationship.Block;
import com.stu.socialnetworkapi.enums.BlockStatus;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BlockRepository extends Neo4jRepository<Block, Long> {
    @Query("""
            OPTIONAL MATCH (a:User {id: $userId})-[r1:BLOCK]->(b:User {id: $targetId})
            OPTIONAL MATCH (c:User {id: $targetId})-[r2:BLOCK]->(d:User {id: $userId})
            WITH
                CASE
                    WHEN r1 IS NOT NULL THEN 'BLOCKED'
                    WHEN r2 IS NOT NULL THEN 'HAS_BEEN_BLOCKED'
                    ELSE 'NORMAL'
                END AS status
            RETURN status
            """)
    BlockStatus getBlockStatus(UUID userId, UUID targetId);

    @Query("""
            MATCH (a:User {username: $username})
            MATCH (b:User {username: $targetUsername})
            
            OPTIONAL MATCH (a)-[f:FRIEND]-(b)
            OPTIONAL MATCH (a)-[r:REQUEST]-(b)
            DELETE f, r
            
            WITH DISTINCT a, b
            CREATE (a)-[:BLOCK {uuid: randomUUID()}]->(b)
            """)
    void blockUser(String username, String targetUsername);

    @Query("""
            MATCH (:User {id: $userId})-[block:BLOCK]->(:User {id: $targetId})
            RETURN block.uuid
            """)
    Optional<UUID> getBlockId(UUID userId, UUID targetId);

    @Query("""
                MATCH (:User)-[r:BLOCK {uuid: $uuid}]->(:User)
                DELETE r
            """)
    void deleteByUuid(UUID uuid);

    @Query("""
            MATCH (blocker:User {username: $username})-[block:BLOCK]->(target:User)
            RETURN target.username AS userId
            LIMIT $limit
            """)
    Set<String> getBlockedUserUsernames(String username, long limit);
}
