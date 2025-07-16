package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.entity.File;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends Neo4jRepository<File, String> {
    @Query("""
            MATCH (uploader:User {username: $uploaderUsername})-[:UPLOAD_FILE]->(f:File)<-[:ATTACH_FILES]-(post:Post)<-[:POSTED]-(author:User)
            OPTIONAL MATCH (viewer)-[friendship:FRIEND]->(author)
            WHERE (
                $viewerUsername = $uploaderUsername
                OR post.privacy = 'PUBLIC'
                OR (post.privacy = 'FRIEND' AND friendship IS NOT NULL)
            )
            RETURN f
            SKIP $skip LIMIT $limit
            """)
    List<File> findFileInPostByUsername(String uploaderUsername, String viewerUsername, long skip, long limit);

}
