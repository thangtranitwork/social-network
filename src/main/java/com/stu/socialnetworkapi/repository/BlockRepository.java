package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.BlockProjection;
import com.stu.socialnetworkapi.entity.relationship.Block;
import com.stu.socialnetworkapi.enums.BlockStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

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
            
                // Xoá quan hệ FRIEND nếu có
                OPTIONAL MATCH (a)-[f1:FRIEND]-(b)
                DELETE f1
            
                WITH a, b, COUNT(f1) AS removedFriends
                SET a.friendCount = a.friendCount - removedFriends,
                    b.friendCount = b.friendCount - removedFriends
            
                WITH a, b, removedFriends
                OPTIONAL MATCH (a)-[r1:REQUEST]->(b)
                OPTIONAL MATCH (a)<-[r2:REQUEST]-(b)
                DELETE r1, r2
            
                WITH a, b, removedFriends, COUNT(r1) AS sentRequestRemoved, COUNT(r2) AS receivedRequestRemoved
                SET a.requestSentCount = a.requestSentCount - sentRequestRemoved,
                    b.requestReceivedCount = b.requestReceivedCount - sentRequestRemoved,
                    a.requestReceivedCount = a.requestReceivedCount - receivedRequestRemoved,
                    b.requestSentCount = b.requestSentCount - receivedRequestRemoved
            
                WITH a, b
                MERGE (a)-[:BLOCK {uuid: randomUUID()}]->(b)
            
                SET a.blockCount = a.blockCount + 1
            """)
    void blockUser(UUID userId, UUID targetId);

    @Query("""
            MATCH (user:User {id: $userId})-[block:BLOCK {uuid: $blockId}]->()
            RETURN COUNT(block) > 0
            """)
    boolean canUnblockUser(UUID blockId, UUID userId);

    @Query("""
            MATCH (blocker:User)-[block:BLOCK {uuid: $blockId}]->()
                    SET blocker.blockCount = CASE
                                              WHEN blocker.blockCount > 0 THEN blocker.blockCount - 1
                                              ELSE 0
                                            END
                    DELETE block
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
    Slice<BlockProjection> getBlockedUsers(UUID userId, Pageable pageable);
}
