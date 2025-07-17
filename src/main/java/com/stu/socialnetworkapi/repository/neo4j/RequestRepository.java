package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.entity.relationship.Request;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RequestRepository extends Neo4jRepository<Request, Long> {
    @Query("""
            MATCH (sender:User {username: $username})-[r:REQUEST]->(target:User)
            RETURN target.username AS username
            LIMIT $limit
            """)
    Set<String> getSentRequestUsernames(String username, long limit);

    @Query("""
            MATCH (sender:User {username: $username})<-[r:REQUEST]-(target:User)
            RETURN target.username AS username
            LIMIT $limit
            """)
    Set<String> getReceivedRequestUsernames(String username, long limit);

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
                MATCH (u:User {username: $username})-[r:REQUEST]-(t:User {username: $targetUsername})
                RETURN r.uuid
            """)
    Optional<UUID> getRequestUUID(String username, String targetUsername);

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

