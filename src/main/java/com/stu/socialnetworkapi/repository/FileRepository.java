package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.File;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends Neo4jRepository<File, String> {
    @Query("""
                MATCH (file:File {id: $fileId})
                OPTIONAL MATCH (currentUser:User {id: $userId})
                RETURN CASE
                    WHEN currentUser IS NOT NULL AND (currentUser)-[:UPLOAD_FILE]->(file) THEN true
                    WHEN file.privacy = 'PUBLIC' THEN true
                    WHEN file.privacy = 'FRIEND' THEN EXISTS {
                        MATCH (currentUser)-[:FRIEND]-(:User)-[:UPLOAD_FILE]->(file)
                    }
                    WHEN file.privacy = 'PRIVATE' THEN false
                    WHEN file.privacy = 'IN_CHAT' THEN EXISTS {
                        MATCH (currentUser)-[:IS_MEMBER_OF]->(:Chat)
                              -[:HAS_MESSAGE]->(:Message)
                              -[:ATTACH_FILES]->(file)
                    }
                    ELSE false
                END AS canRead
            """)
    boolean canUserReadFile(String fileId, UUID userId);

    @Query("""
            OPTIONAL MATCH (f:File {id: $id})
            RETURN f.name
            """)
    Optional<String> getNameById(String id);
}
