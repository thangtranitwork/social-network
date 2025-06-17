package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.enums.PostPrivacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends Neo4jRepository<File, String> {
    @Query("""
            MATCH (uploader:User {id: $userId})-[:UPLOAD_FILE]->(f:File)<-[:ATTACH_FILES]-(p:Post)
            WHERE p.privacy IN $privacies
            RETURN f
            SKIP $skip LIMIT $limit
            """)
    List<File> findFileInPostByUserIdAndPrivacyIsIn(UUID userId, List<PostPrivacy> privacies, Pageable pageable);

}
