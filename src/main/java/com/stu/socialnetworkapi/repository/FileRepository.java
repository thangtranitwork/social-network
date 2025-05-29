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
                MATCH (file:File {id: $fileId})<-[:UPLOAD_FILE]-(uploader:User)
                OPTIONAL MATCH (currentUser:User {id: $userId})
                OPTIONAL MATCH (currentUser)-[block:BLOCKED]-(uploader)
                OPTIONAL MATCH (file)<-[:ATTACH_FILES]-(post:Post)
                RETURN CASE
                    WHEN currentUser IS NOT NULL AND currentUser.id = uploader.id THEN true
                    WHEN block IS NOT NULL THEN false
                    WHEN file.privacy = 'PUBLIC' THEN true
                    WHEN file.privacy = 'FRIEND' THEN EXISTS {
                        MATCH (currentUser)-[:FRIEND]-(uploader)
                    }
                    WHEN file.privacy = 'PRIVATE' THEN false
                    WHEN file.privacy = 'IN_CHAT' THEN EXISTS {
                        MATCH (currentUser)-[:IS_MEMBER_OF]->(:Chat)
                              -[:HAS_MESSAGE]->(:Message)
                              -[:ATTACH_FILES]->(file)
                    }
                    WHEN file.privacy = 'IN_POST' AND post IS NOT NULL AND post.privacy = 'FRIEND' THEN EXISTS {
                        MATCH (currentUser)-[:FRIEND]-(uploader)
                    }
                    WHEN file.privacy = 'IN_POST' AND post IS NOT NULL AND post.privacy = 'PRIVATE' THEN false
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
