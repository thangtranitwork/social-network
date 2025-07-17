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
                SET sender.requestSentCount = sender.requestSentCount + 1,
                    target.requestReceivedCount = target.requestReceivedCount + 1
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
                MATCH (a:User)-[r:REQUEST {uuid: $uuid}]->(b:User)
                SET a.requestSentCount = a.requestSentCount - 1,
                    b.requestReceivedCount = b.requestReceivedCount - 1
                DELETE r
            """)
    void deleteByUuid(UUID uuid);

    @Query("""
                MATCH (sender:User)-[r:REQUEST {uuid: $requestId}]->(target:User)
                SET sender.requestSentCount = coalesce(sender.requestSentCount, 0) - 1,
                    sender.friendCount = coalesce(sender.friendCount, 0) + 1,
                    target.requestReceivedCount = coalesce(target.requestReceivedCount, 0) - 1,
                    target.friendCount = coalesce(target.friendCount, 0) + 1
                DELETE r
                CREATE (sender)-[:FRIEND {uuid: randomUUID(), createdAt: datetime()}]->(target)
                CREATE (sender)<-[:FRIEND {uuid: randomUUID(), createdAt: datetime()}]-(target)
                RETURN true
            """)
    boolean acceptRequest(UUID requestId);

    @Query("""
                MATCH (sender:User)-[r:REQUEST {uuid: $uuid}]->(target:User)
                DELETE r
            """)
    void delete(UUID uuid);
}

