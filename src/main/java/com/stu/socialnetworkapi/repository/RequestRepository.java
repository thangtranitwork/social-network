package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.dto.projection.UserProjection;
import com.stu.socialnetworkapi.entity.relationship.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRepository extends Neo4jRepository<Request, Long> {
    @Query("""
             MATCH (sender:User {id: $userId})-[r:REQUEST]->(target:User)
             OPTIONAL MATCH (target)-[:HAS_PROFILE_PICTURE]->(profile: File)
             RETURN r.sentAt AS sentAt,
                    target.id AS userId,
                    target.givenName AS givenName,
                    target.familyName AS familyName,
                    target.username AS username,
                    profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    List<UserProjection> getSentRequest(UUID userId, Pageable pageable);

    @Query("""
             MATCH (sender:User)-[r:REQUEST]->(target:User {id: $userId})
             OPTIONAL MATCH (sender)-[:HAS_PROFILE_PICTURE]->(profile: File)
             RETURN r.sentAt AS sentAt,
                    sender.id AS userId,
                    sender.givenName AS givenName,
                    sender.familyName AS familyName,
                    sender.username AS username,
                    profile.id AS profilePictureId
            SKIP $skip LIMIT $limit
            """)
    List<UserProjection> getReceivedRequest(UUID userId, Pageable pageable);

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
            """)
    void create(UUID senderId, UUID targetId);

    @Query("""
                MATCH (u:User {id: $userId})-[r:REQUEST]-(t:User {id: $targetId})
                RETURN r.uuid
            """)
    Optional<UUID> getRequestUUID(UUID userId, UUID targetId);

    @Query("""
                MATCH (u:User {id: $senderId})-[r:REQUEST]->(t:User {id: $targetId})
                RETURN r.uuid
            """)
    Optional<UUID> getRequestUUIDWhichDirection(UUID senderId, UUID targetId);

    @Query("""
                MATCH (:User)-[r:REQUEST {uuid: $uuid}]-(:User)
                DELETE r
            """)
    void deleteByUuid(UUID uuid);

    @Query("""
                MATCH (sender:User)-[r:REQUEST {uuid: $requestId}]->(target:User)
                WITH sender, target, r
                DELETE r
                MERGE (sender)-[friend:FRIEND {uuid: randomUUID(), createdAt: datetime()}]->(target)
                MERGE (sender)<-[friendReverse:FRIEND {uuid: randomUUID(), createdAt: datetime()}]-(target)
            """)
    void acceptRequest(UUID requestId);

    @Query("""
                MATCH (sender:User)-[r:REQUEST {uuid: $uuid}]->(target:User)
                DELETE r
            """)
    void delete(UUID uuid);
}

