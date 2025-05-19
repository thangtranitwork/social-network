package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.RequestProjection;
import com.stu.socialnetworkapi.entity.relationship.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestRepository extends Neo4jRepository<Request, Long> {

    @Query("""
             MATCH (sender:User {id: $userId})-[r:REQUEST]->(target:User)
             OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(profile: File)
             RETURN r.uuid AS requestId,
                    r.sentAt AS sentAt,
                    target.id AS userId,
                    target.givenName AS givenName,
                    target.familyName AS familyName,
                    target.username AS username,
                    profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    Slice<RequestProjection> getSentRequest(UUID userId, Pageable pageable);

    @Query("""
             MATCH (sender:User)-[r:REQUEST]->(target:User {id: $userId})
             OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(profile: File)
             RETURN r.uuid AS requestId,
                    r.sentAt AS sentAt,
                    sender.id AS userId,
                    sender.givenName AS givenName,
                    sender.familyName AS familyName,
                    sender.username AS username,
                    profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    Slice<RequestProjection> getReceivedRequest(UUID userId, Pageable pageable);

    @Query("""
                MATCH (a:User {id: $senderId})
                MATCH (b:User {id: $targetId})
                RETURN NOT EXISTS {
                    MATCH (a)-[:REQUEST|FRIEND|BLOCK]-(b)
                }
            """)
    boolean canSendRequest(UUID senderId, UUID targetId);

    @Query("""
            MATCH (sender:User {id: $senderId})
            MATCH (target:User {id: $targetId})
            CREATE (sender)-[:REQUEST {
                sentAt: datetime(),
                uuid: randomUUID()
            }]->(target)
            SET sender.requestSentCount = coalesce(sender.requestSentCount, 0) + 1,
                target.requestReceivedCount = coalesce(target.requestReceivedCount, 0) + 1
            """)
    void create(UUID senderId, UUID targetId);


    @Query("""
                MATCH (sender:User)-[r:REQUEST]->(target:User)
                WHERE r.uuid = $requestId AND (sender.id = $userId OR target.id = $userId)
                RETURN COUNT(r) > 0
            """)
    boolean canDeleteRequest(UUID requestId, UUID userId);

    @Query("""
            MATCH (sender:User)-[request:REQUEST {uuid: $requestId}]->(target:User)
            WITH sender, target
            OPTIONAL MATCH (sender)-[old:REQUEST]->(target)
            DELETE old
            WITH sender, target
            CREATE (sender)-[friend:FRIEND {uuid: randomUUID(), createdAt: datetime()}]->(target),
                   (sender)<-[friendReverse:FRIEND {uuid: randomUUID(), createdAt: datetime()}]-(target)
            SET sender.friendCount = COALESCE(sender.friendCount, 0) + 1,
                target.friendCount = COALESCE(target.friendCount, 0) + 1,
                sender.requestSentCount = coalesce(sender.requestSentCount, 0) + 1,
                target.requestReceivedCount = coalesce(target.requestReceivedCount, 0) + 1
            """)
    void acceptRequest(UUID requestId);

    @Query("""
        MATCH (sender:User)-[r:REQUEST {uuid: $uuid}]->(receiver:User)
        WITH sender, receiver, r
        SET sender.requestSentCount = CASE
                                        WHEN sender.requestSentCount > 0 THEN sender.requestSentCount - 1
                                        ELSE 0
                                      END,
            receiver.requestReceivedCount = CASE
                                             WHEN receiver.requestReceivedCount > 0 THEN receiver.requestReceivedCount - 1
                                             ELSE 0
                                           END
        DELETE r
        """)
    void delete(UUID uuid);

    @Query("""
            OPTIONAL MATCH (sender:User)-[r:REQUEST {uuid: $uuid}]->(target:User)
            RETURN target.id
            """)
    UUID getTargetId(UUID uuid);

    @Query("""
            OPTIONAL MATCH (sender:User)-[r:REQUEST {uuid: $uuid}]->(target:User)
            RETURN sender.id
            """)
    UUID getSenderId(UUID uuid);

    @Query("""
            MATCH ()-[r:REQUEST {uuid: $uuid}]->()
            RETURN count(r) > 0
            """)
    boolean existsByUuid(UUID uuid);

}

