package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.entity.Keyword;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KeywordRepository extends Neo4jRepository<Keyword, String> {
    @Query("""
                MATCH (user:User {id: $userId})
                MATCH (post:Post {id: $postId})-[:HAS_KEYWORDS]->(keyword:Keyword)
                MERGE (user)-[i:INTERACT_WITH]->(keyword)
                ON CREATE SET i.score = $weight, keyword.score = keyword.score + $weight
                ON MATCH SET i.score = i.score + $weight, keyword.score = keyword.score + $weight
            """)
    void interact(UUID postId, UUID userId, int weight);

    @Query("""
                MATCH (user:User {username: $username})
                MATCH (post:Post {id: $postId})-[:HAS_KEYWORDS]->(keyword:Keyword)
                MERGE (user)-[i:INTERACT_WITH]->(keyword)
                ON CREATE SET i.score = $weight, keyword.score = keyword.score + $weight
                ON MATCH SET i.score = i.score + $weight, keyword.score = keyword.score + $weight
            """)
    void interact(UUID postId, String username, int weight);

    @Query("""
                UNWIND $keywords AS keyword
                MATCH (post:Post {id: $postId})
                MERGE (k:Keyword {text: keyword})
                MERGE (post)-[:HAS_KEYWORDS]->(k)
            """)
    void save(UUID postId, List<String> keywords);

}
