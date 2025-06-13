package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.BlockProjection;
import com.stu.socialnetworkapi.entity.relationship.Block;
import com.stu.socialnetworkapi.enums.BlockStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
                MATCH (a:User {id: $userId})
                MATCH (b:User {id: $targetId})
            
                OPTIONAL MATCH (a)-[f1:FRIEND]-(b)
                OPTIONAL MATCH (a)-[r1:REQUEST]->(b)
                OPTIONAL MATCH (a)<-[r2:REQUEST]-(b)
                DELETE f1, r1, r2
            
                MERGE (a)-[:BLOCK {uuid: randomUUID()}]->(b)
            
                WITH a, b
                CALL {
                    WITH a
                    OPTIONAL MATCH (a)-[:FRIEND]-(:User)
                    WITH COUNT(*) AS aFriendCount
                    OPTIONAL MATCH (a)-[:REQUEST]->(:User)
                    WITH aFriendCount, COUNT(*) AS aRequestSentCount
                    OPTIONAL MATCH (a)<-[:REQUEST]-(:User)
                    WITH aFriendCount, aRequestSentCount, COUNT(*) AS aRequestReceivedCount
                    OPTIONAL MATCH (a)-[:BLOCK]->(:User)
                    WITH aFriendCount, aRequestSentCount, aRequestReceivedCount, COUNT(*) AS aBlockCount
                    RETURN aFriendCount, aRequestSentCount, aRequestReceivedCount, aBlockCount
                }
            
                WITH a, b, aFriendCount, aRequestSentCount, aRequestReceivedCount, aBlockCount
                CALL {
                    WITH b
                    OPTIONAL MATCH (b)-[:FRIEND]-(:User)
                    WITH COUNT(*) AS bFriendCount
                    OPTIONAL MATCH (b)-[:REQUEST]->(:User)
                    WITH bFriendCount, COUNT(*) AS bRequestSentCount
                    OPTIONAL MATCH (b)<-[:REQUEST]-(:User)
                    WITH bFriendCount, bRequestSentCount, COUNT(*) AS bRequestReceivedCount
                    RETURN bFriendCount, bRequestSentCount, bRequestReceivedCount
                }
            
                // Update counts
                SET a.friendCount = aFriendCount,
                    a.requestSentCount = aRequestSentCount,
                    a.requestReceivedCount = aRequestReceivedCount,
                    a.blockCount = aBlockCount,
                    b.friendCount = bFriendCount,
                    b.requestSentCount = bRequestSentCount,
                    b.requestReceivedCount = bRequestReceivedCount
            """)
    void blockUser(UUID userId, UUID targetId);

    @Query("""
            MATCH (user:User {id: $userId})-[block:BLOCK {uuid: $blockId}]->()
            RETURN COUNT(block) > 0
            """)
    boolean canUnblockUser(UUID blockId, UUID userId);

    @Query("""
            MATCH (blocker:User)-[block:BLOCK {uuid: $blockId}]->()
            DELETE block
            WITH blocker
            CALL {
                WITH blocker
                OPTIONAL MATCH (blocker)-[:BLOCK]->(:User)
                RETURN COUNT(*) AS blockerBlockCount
            }
            SET blocker.blockCount = blockerBlockCount
            """)
    void unblockUser(UUID blockId);

    @Query("""
            MATCH (blocker:User {id: $userId})-[block:BLOCK]->(target:User)
            OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(profile: File)
            RETURN block.uuid AS blockId,
                   target.id AS userId,
                   target.givenName AS givenName,
                   target.familyName AS familyName,
                   target.username AS username,
                   profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    List<BlockProjection> getBlockedUsers(UUID userId, Pageable pageable);
}
