package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Keyword;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KeywordRepository extends Neo4jRepository<Keyword, String> {
    @Query("""
                MATCH (user:User {id: $userId})
                MATCH (post:Post {id: $postId})-[:HAS_KEYWORDS]->(keyword:Keyword)
                MERGE (user)-[i:INTERACT_WITH]->(keyword)
                ON CREATE SET i.score = $weight
                ON MATCH SET i.score = i.score + $weight
            """)
    void interact(UUID postId, UUID userId, int weight);
}
