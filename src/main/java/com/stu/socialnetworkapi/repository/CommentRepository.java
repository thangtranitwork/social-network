package com.stu.socialnetworkapi.repository;

import com.stu.socialnetworkapi.entity.Comment;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends Neo4jRepository<Comment, UUID> {
    @Query("""
            MATCH (c:Comment {id: $commentId})<-[like:LIKED]-(u:User {id: $likerId})
            RETURN COUNT(like) > 0
            """)
    boolean isLiked(UUID commentId, UUID likerId);

    List<Comment> findAllByPostId(UUID postId, Pageable pageable);
}
